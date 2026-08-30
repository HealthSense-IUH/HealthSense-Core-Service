package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public class V6__final_summary_closure extends BaseJavaMigration {

    private static final String SESSIONS = "consultation_sessions";
    private static final String SUMMARIES = "consultation_final_summaries";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, SESSIONS))
            return;

        addClosureColumns(connection);
        if (tableExists(connection, SUMMARIES)) {
            addSummaryVersion(connection);
            createAddendaTable(connection);
            createSummaryHealthRecordReferenceTable(connection);
        }
        backfillCompletedEpisodes(connection);
    }

    private void addSummaryVersion(Connection connection) throws SQLException {
        if (!columnExists(connection, SUMMARIES, "version")) {
            execute(connection, "ALTER TABLE consultation_final_summaries ADD COLUMN version BIGINT DEFAULT 0");
            execute(connection, "UPDATE consultation_final_summaries SET version = 0 WHERE version IS NULL");
            execute(connection, "ALTER TABLE consultation_final_summaries ALTER COLUMN version SET NOT NULL");
        }
    }

    private void addClosureColumns(Connection connection) throws SQLException {
        addColumnIfMissing(connection, SESSIONS, "summary_closure_status", "VARCHAR(40)");
        addColumnIfMissing(connection, SESSIONS, "summary_due_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, SESSIONS, "summary_escalated_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, SESSIONS, "summary_escalation_reason", "VARCHAR(1000)");
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_session_summary_closure
                ON consultation_sessions (summary_closure_status, summary_due_at)
                """);
    }

    private void createAddendaTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_final_summary_addenda (
                    id BIGINT NOT NULL,
                    summary_id BIGINT NOT NULL,
                    session_id BIGINT NOT NULL,
                    author_doctor_id BIGINT NOT NULL,
                    reason VARCHAR(1000) NOT NULL,
                    content TEXT NOT NULL,
                    authored_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_final_summary_addenda PRIMARY KEY (id),
                    CONSTRAINT fk_summary_addendum_summary FOREIGN KEY (summary_id)
                        REFERENCES consultation_final_summaries (id),
                    CONSTRAINT fk_summary_addendum_session FOREIGN KEY (session_id)
                        REFERENCES consultation_sessions (id)
                )
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_summary_addendum_summary_created
                ON consultation_final_summary_addenda (summary_id, created_at)
                """);
    }

    private void createSummaryHealthRecordReferenceTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_final_summary_health_records (
                    summary_id BIGINT NOT NULL,
                    health_record_id BIGINT NOT NULL,
                    CONSTRAINT pk_final_summary_health_records
                        PRIMARY KEY (summary_id, health_record_id),
                    CONSTRAINT fk_final_summary_health_record_summary FOREIGN KEY (summary_id)
                        REFERENCES consultation_final_summaries (id)
                )
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_final_summary_health_record
                ON consultation_final_summary_health_records (health_record_id)
                """);
    }

    private void backfillCompletedEpisodes(Connection connection) throws SQLException {
        boolean summariesExist = tableExists(connection, SUMMARIES);
        boolean usersExist = tableExists(connection, "user_accounts");
        String query = """
                SELECT id, doctor_id, COALESCE(completed_at, ends_at, CURRENT_TIMESTAMP) AS closure_time
                FROM consultation_sessions
                WHERE status = 'COMPLETED' AND summary_closure_status IS NULL
                """;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query);
             PreparedStatement update = connection.prepareStatement("""
                     UPDATE consultation_sessions
                     SET summary_closure_status = ?, summary_due_at = ?,
                         summary_escalated_at = ?, summary_escalation_reason = ?
                     WHERE id = ?
                     """)) {
            while (result.next()) {
                long sessionId = result.getLong("id");
                long doctorId = result.getLong("doctor_id");
                Instant closureTime = result.getTimestamp("closure_time").toInstant();
                Instant dueAt = closureTime.plus(Duration.ofHours(24));
                boolean finalized = summariesExist && hasFinalizedSummary(connection, sessionId);
                boolean activeDoctor = !usersExist || isActiveDoctor(connection, doctorId);
                String status;
                Instant escalatedAt = null;
                String reason = null;
                if (finalized) {
                    status = "SUMMARY_FINALIZED";
                } else if (!activeDoctor) {
                    status = "ESCALATED";
                    escalatedAt = Instant.now();
                    reason = "Assigned Doctor is not active before Final Care Summary finalization";
                } else if (!dueAt.isAfter(Instant.now())) {
                    status = "SUMMARY_OVERDUE";
                } else {
                    status = "SUMMARY_PENDING";
                }

                update.setString(1, status);
                update.setTimestamp(2, Timestamp.from(dueAt));
                if (escalatedAt == null) update.setNull(3, Types.TIMESTAMP_WITH_TIMEZONE);
                else update.setTimestamp(3, Timestamp.from(escalatedAt));
                update.setString(4, reason);
                update.setLong(5, sessionId);
                update.addBatch();
            }
            update.executeBatch();
        }
    }

    private boolean hasFinalizedSummary(Connection connection, long sessionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM consultation_final_summaries WHERE session_id = ? AND status = 'FINALIZED'")) {
            statement.setLong(1, sessionId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private boolean isActiveDoctor(Connection connection, long doctorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM user_accounts WHERE id = ? AND role = 'DOCTOR' AND status = 'ACTIVE'")) {
            statement.setLong(1, doctorId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void addColumnIfMissing(
            Connection connection, String table, String column, String definition) throws SQLException {
        if (!columnExists(connection, table, column))
            execute(connection, "ALTER TABLE %s ADD COLUMN %s %s".formatted(table, column, definition));
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (result.next()) return true;
        }
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null,
                table.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            if (result.next()) return true;
        }
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null,
                table.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT))) {
            return result.next();
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
