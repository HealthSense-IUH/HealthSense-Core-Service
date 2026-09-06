package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V11__queue_dispatch_domain_foundation extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        addRequestFoundation(connection);
        addDoctorFoundation(connection);
        addSessionFoundation(connection);
        createQueueFoundation(connection);
        createDispatchState(connection);
    }

    private void addRequestFoundation(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_requests")) return;

        addColumnIfMissing(connection, "consultation_requests", "flow_type", "VARCHAR(30)");
        execute(connection, "UPDATE consultation_requests SET flow_type = 'LEGACY_V3' WHERE flow_type IS NULL");
        setNotNull(connection, "consultation_requests", "flow_type");
        setDefault(connection, "consultation_requests", "flow_type", "'LEGACY_V3'");

        execute(connection, "ALTER TABLE consultation_requests DROP CONSTRAINT IF EXISTS consultation_requests_status_check");
        execute(connection, """
                ALTER TABLE consultation_requests
                ADD CONSTRAINT consultation_requests_status_check
                CHECK (status IN (
                    'PENDING_REVIEW', 'NEED_MORE_INFO', 'WAITING_ACCEPTANCE', 'WAITING_PAYMENT',
                    'QUEUED', 'FULFILLED', 'REJECTED', 'CANCELLED', 'EXPIRED', 'TIMED_OUT'
                ))
                """);
        addCheckIfMissing(connection, "consultation_requests", "ck_consultation_request_flow_type",
                "flow_type IN ('LEGACY_V3', 'QUEUE_DISPATCH_V1')");
    }

    private void addDoctorFoundation(Connection connection) throws SQLException {
        if (!tableExists(connection, "doctor_care_profiles")) return;

        addColumnIfMissing(connection, "doctor_care_profiles", "dispatch_status", "VARCHAR(20)");
        addColumnIfMissing(connection, "doctor_care_profiles", "stop_after_current_session", "BOOLEAN");
        addColumnIfMissing(connection, "doctor_care_profiles", "busy_session_id", "BIGINT");
        addColumnIfMissing(connection, "doctor_care_profiles", "dispatch_status_changed_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "doctor_care_profiles", "version", "BIGINT");

        execute(connection, "UPDATE doctor_care_profiles SET dispatch_status = 'UNAVAILABLE' WHERE dispatch_status IS NULL");
        execute(connection, "UPDATE doctor_care_profiles SET stop_after_current_session = FALSE WHERE stop_after_current_session IS NULL");
        execute(connection, "UPDATE doctor_care_profiles SET dispatch_status_changed_at = CURRENT_TIMESTAMP WHERE dispatch_status_changed_at IS NULL");
        execute(connection, "UPDATE doctor_care_profiles SET version = 0 WHERE version IS NULL");

        setNotNull(connection, "doctor_care_profiles", "dispatch_status");
        setNotNull(connection, "doctor_care_profiles", "stop_after_current_session");
        setNotNull(connection, "doctor_care_profiles", "dispatch_status_changed_at");
        setNotNull(connection, "doctor_care_profiles", "version");
        setDefault(connection, "doctor_care_profiles", "dispatch_status", "'UNAVAILABLE'");
        setDefault(connection, "doctor_care_profiles", "stop_after_current_session", "FALSE");
        setDefault(connection, "doctor_care_profiles", "dispatch_status_changed_at", "CURRENT_TIMESTAMP");
        setDefault(connection, "doctor_care_profiles", "version", "0");

        addCheckIfMissing(connection, "doctor_care_profiles", "ck_doctor_dispatch_status",
                "dispatch_status IN ('UNAVAILABLE', 'AVAILABLE', 'BUSY')");
        addCheckIfMissing(connection, "doctor_care_profiles", "ck_doctor_busy_session_ownership",
                "(dispatch_status = 'BUSY' AND busy_session_id IS NOT NULL) OR " +
                        "(dispatch_status IN ('UNAVAILABLE', 'AVAILABLE') AND busy_session_id IS NULL)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_doctor_dispatch_status ON doctor_care_profiles (dispatch_status, doctor_id)");

        if (tableExists(connection, "consultation_sessions")
                && !constraintExists(connection, "doctor_care_profiles", "fk_doctor_busy_session")) {
            execute(connection, """
                    ALTER TABLE doctor_care_profiles
                    ADD CONSTRAINT fk_doctor_busy_session
                    FOREIGN KEY (busy_session_id) REFERENCES consultation_sessions (id)
                    """);
        }
    }

    private void addSessionFoundation(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_sessions")) return;

        addColumnIfMissing(connection, "consultation_sessions", "flow_type", "VARCHAR(30)");
        addColumnIfMissing(connection, "consultation_sessions", "continuation_round", "INTEGER");
        addColumnIfMissing(connection, "consultation_sessions", "block_started_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_sessions", "doctor_released_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_sessions", "doctor_release_reason", "VARCHAR(40)");
        addColumnIfMissing(connection, "consultation_sessions", "version", "BIGINT");

        execute(connection, "UPDATE consultation_sessions SET flow_type = 'LEGACY_V3' WHERE flow_type IS NULL");
        execute(connection, "UPDATE consultation_sessions SET continuation_round = 0 WHERE continuation_round IS NULL");
        execute(connection, "UPDATE consultation_sessions SET version = 0 WHERE version IS NULL");
        setNotNull(connection, "consultation_sessions", "flow_type");
        setNotNull(connection, "consultation_sessions", "continuation_round");
        setNotNull(connection, "consultation_sessions", "version");
        setDefault(connection, "consultation_sessions", "flow_type", "'LEGACY_V3'");
        setDefault(connection, "consultation_sessions", "continuation_round", "0");
        setDefault(connection, "consultation_sessions", "version", "0");

        addCheckIfMissing(connection, "consultation_sessions", "ck_consultation_session_flow_type",
                "flow_type IN ('LEGACY_V3', 'QUEUE_DISPATCH_V1')");
        addCheckIfMissing(connection, "consultation_sessions", "ck_consultation_session_round",
                "continuation_round >= 0");
        addCheckIfMissing(connection, "consultation_sessions", "ck_consultation_session_release_reason",
                "doctor_release_reason IS NULL OR doctor_release_reason IN ('SUMMARY_FINALIZED', 'SUMMARY_TIMEOUT')");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_queue_session_block_end ON consultation_sessions (flow_type, status, ends_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_queue_session_summary_release ON consultation_sessions (flow_type, doctor_released_at, summary_due_at)");

        if (isPostgreSql(connection)) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_queue_active_session_doctor
                    ON consultation_sessions (doctor_id)
                    WHERE flow_type = 'QUEUE_DISPATCH_V1' AND status = 'ACTIVE'
                    """);
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_queue_active_session_member
                    ON consultation_sessions (member_id)
                    WHERE flow_type = 'QUEUE_DISPATCH_V1' AND status = 'ACTIVE'
                    """);
        }
    }

    private void createQueueFoundation(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_requests")) return;

        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_queue_entries (
                    id BIGINT NOT NULL,
                    request_id BIGINT NOT NULL,
                    member_id BIGINT NOT NULL,
                    queue_date DATE NOT NULL,
                    queue_number BIGINT NOT NULL,
                    status VARCHAR(40) NOT NULL,
                    queued_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    cancelled_at TIMESTAMP WITH TIME ZONE,
                    timed_out_at TIMESTAMP WITH TIME ZONE,
                    fulfilled_at TIMESTAMP WITH TIME ZONE,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_queue_entries PRIMARY KEY (id),
                    CONSTRAINT uq_queue_entry_request UNIQUE (request_id),
                    CONSTRAINT uq_queue_entry_daily_number UNIQUE (queue_date, queue_number),
                    CONSTRAINT fk_queue_entry_request FOREIGN KEY (request_id) REFERENCES consultation_requests (id),
                    CONSTRAINT ck_queue_entry_number CHECK (queue_number > 0),
                    CONSTRAINT ck_queue_entry_status CHECK (status IN (
                        'WAITING', 'OFFERING_DOCTOR', 'WAITING_MEMBER_CONFIRMATION',
                        'FULFILLED', 'CANCELLED', 'TIMED_OUT'
                    ))
                )
                """);
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_queue_fifo ON consultation_queue_entries (status, queue_date, queue_number)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_queue_member_status ON consultation_queue_entries (member_id, status)");

        if (isPostgreSql(connection)) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_queue_one_unresolved_per_member
                    ON consultation_queue_entries (member_id)
                    WHERE status IN ('WAITING', 'OFFERING_DOCTOR', 'WAITING_MEMBER_CONFIRMATION')
                    """);
        }

        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_queue_counter (
                    queue_date DATE NOT NULL,
                    last_number BIGINT NOT NULL DEFAULT 0,
                    version BIGINT NOT NULL DEFAULT 0,
                    CONSTRAINT pk_consultation_queue_counter PRIMARY KEY (queue_date),
                    CONSTRAINT ck_queue_counter_non_negative CHECK (last_number >= 0)
                )
                """);
    }

    private void createDispatchState(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_dispatch_state (
                    id BIGINT NOT NULL,
                    last_doctor_id BIGINT,
                    version BIGINT NOT NULL DEFAULT 0,
                    CONSTRAINT pk_consultation_dispatch_state PRIMARY KEY (id),
                    CONSTRAINT ck_consultation_dispatch_singleton CHECK (id = 1)
                )
                """);
        execute(connection, """
                INSERT INTO consultation_dispatch_state (id, last_doctor_id, version)
                SELECT 1, NULL, 0
                WHERE NOT EXISTS (SELECT 1 FROM consultation_dispatch_state WHERE id = 1)
                """);
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String definition)
            throws SQLException {
        if (!columnExists(connection, table, column))
            execute(connection, "ALTER TABLE %s ADD COLUMN %s %s".formatted(table, column, definition));
    }

    private void addCheckIfMissing(Connection connection, String table, String name, String expression)
            throws SQLException {
        if (!constraintExists(connection, table, name))
            execute(connection, "ALTER TABLE %s ADD CONSTRAINT %s CHECK (%s)".formatted(table, name, expression));
    }

    private void setNotNull(Connection connection, String table, String column) throws SQLException {
        execute(connection, "ALTER TABLE %s ALTER COLUMN %s SET NOT NULL".formatted(table, column));
    }

    private void setDefault(Connection connection, String table, String column, String value) throws SQLException {
        execute(connection, "ALTER TABLE %s ALTER COLUMN %s SET DEFAULT %s".formatted(table, column, value));
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

    private boolean constraintExists(Connection connection, String table, String constraint) throws SQLException {
        String query = "SELECT constraint_name FROM information_schema.table_constraints " +
                "WHERE LOWER(table_name) = LOWER('%s') AND LOWER(constraint_name) = LOWER('%s')"
                        .formatted(table.replace("'", "''"), constraint.replace("'", "''"));
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            return result.next();
        }
    }

    private boolean isPostgreSql(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgresql");
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
