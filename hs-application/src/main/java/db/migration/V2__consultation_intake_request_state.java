package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V2__consultation_intake_request_state extends BaseJavaMigration {

    static final String REQUESTS = "consultation_requests";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, REQUESTS))
            return;

        addIntakeColumns(connection);
        createSelectedRecordTable(connection);
        createMoreInfoHistoryTables(connection);
        migrateLegacyIntake(connection);
        migrateLegacyMoreInfoHistory(connection);
        addIntegrityIndexes(connection);
    }

    private void addIntakeColumns(Connection connection) throws SQLException {
        addColumnIfMissing(connection, "reason_for_care", "VARCHAR(1000)");
        addColumnIfMissing(connection, "current_concern", "VARCHAR(2000)");
        addColumnIfMissing(connection, "care_goal", "VARCHAR(1000)");
        addColumnIfMissing(connection, "member_note", "VARCHAR(1000)");
        addColumnIfMissing(connection, "relevant_self_reported_context", "VARCHAR(4000)");
        addColumnIfMissing(connection, "intake_frozen_at", "TIMESTAMP WITH TIME ZONE");
    }

    private void createSelectedRecordTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_request_health_records (
                    request_id BIGINT NOT NULL,
                    selection_order INTEGER NOT NULL,
                    health_record_id BIGINT NOT NULL,
                    CONSTRAINT pk_consultation_request_health_records
                        PRIMARY KEY (request_id, selection_order),
                    CONSTRAINT fk_consultation_request_health_records_request
                        FOREIGN KEY (request_id) REFERENCES consultation_requests (id)
                )
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_request_health_record_reference
                ON consultation_request_health_records (health_record_id)
                """);
    }

    private void createMoreInfoHistoryTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_more_info_cycles (
                    id BIGINT NOT NULL,
                    request_id BIGINT NOT NULL,
                    requested_items_category VARCHAR(120),
                    coordinator_message VARCHAR(1000) NOT NULL,
                    requested_by BIGINT NOT NULL,
                    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    member_response VARCHAR(2000),
                    responded_at TIMESTAMP WITH TIME ZONE,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_more_info_cycles PRIMARY KEY (id),
                    CONSTRAINT fk_more_info_cycle_request
                        FOREIGN KEY (request_id) REFERENCES consultation_requests (id)
                )
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_more_info_cycle_request
                ON consultation_more_info_cycles (request_id, requested_at)
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_more_info_response_records (
                    cycle_id BIGINT NOT NULL,
                    reference_order INTEGER NOT NULL,
                    health_record_id BIGINT NOT NULL,
                    CONSTRAINT pk_more_info_response_records
                        PRIMARY KEY (cycle_id, reference_order),
                    CONSTRAINT fk_more_info_response_records_cycle
                        FOREIGN KEY (cycle_id) REFERENCES consultation_more_info_cycles (id)
                )
                """);
    }

    private void migrateLegacyIntake(Connection connection) throws SQLException {
        if (columnExists(connection, REQUESTS, "reason")) {
            execute(connection, """
                    UPDATE consultation_requests
                    SET reason_for_care = reason
                    WHERE reason_for_care IS NULL
                      AND reason IS NOT NULL
                    """);
        }
        if (columnExists(connection, REQUESTS, "member_additional_note")) {
            execute(connection, """
                    UPDATE consultation_requests
                    SET member_note = member_additional_note
                    WHERE member_note IS NULL
                      AND member_additional_note IS NOT NULL
                    """);
        }
        if (columnExists(connection, REQUESTS, "health_record_id")) {
            execute(connection, """
                    INSERT INTO consultation_request_health_records
                        (request_id, selection_order, health_record_id)
                    SELECT r.id, 0, r.health_record_id
                    FROM consultation_requests r
                    WHERE r.health_record_id IS NOT NULL
                      AND NOT EXISTS (
                          SELECT 1
                          FROM consultation_request_health_records selected
                          WHERE selected.request_id = r.id
                      )
                    """);
        }
        if (columnExists(connection, REQUESTS, "doctor_reserved_at")
                && columnExists(connection, REQUESTS, "reviewed_at")
                && columnExists(connection, REQUESTS, "status")) {
            execute(connection, """
                    UPDATE consultation_requests
                    SET intake_frozen_at = COALESCE(doctor_reserved_at, reviewed_at)
                    WHERE intake_frozen_at IS NULL
                      AND status IN ('WAITING_ACCEPTANCE', 'WAITING_PAYMENT', 'FULFILLED')
                      AND COALESCE(doctor_reserved_at, reviewed_at) IS NOT NULL
                    """);
        }
    }

    private void migrateLegacyMoreInfoHistory(Connection connection) throws SQLException {
        if (!columnExists(connection, REQUESTS, "more_info_reason")
                || !columnExists(connection, REQUESTS, "reviewed_by_admin_id")
                || !columnExists(connection, REQUESTS, "reviewed_at"))
            return;

        boolean hasMemberResponse = columnExists(connection, REQUESTS, "member_additional_note");
        String memberResponse = hasMemberResponse
                ? "r.member_additional_note"
                : "NULL";
        String respondedAt = hasMemberResponse && columnExists(connection, REQUESTS, "updated_at")
                ? "CASE WHEN r.member_additional_note IS NOT NULL THEN r.updated_at ELSE NULL END"
                : "NULL";
        execute(connection, """
                INSERT INTO consultation_more_info_cycles (
                    id, request_id, coordinator_message, requested_by, requested_at,
                    member_response, responded_at, created_at, updated_at
                )
                SELECT r.id, r.id, r.more_info_reason, r.reviewed_by_admin_id, r.reviewed_at,
                       %s, %s, r.reviewed_at, r.reviewed_at
                FROM consultation_requests r
                WHERE r.more_info_reason IS NOT NULL
                  AND r.reviewed_by_admin_id IS NOT NULL
                  AND r.reviewed_at IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM consultation_more_info_cycles cycle WHERE cycle.request_id = r.id
                  )
                """.formatted(memberResponse, respondedAt));
    }

    private void addIntegrityIndexes(Connection connection) throws SQLException {
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_request_member_status_v3
                ON consultation_requests (member_id, status)
                """);
        if (isPostgreSql(connection) && !hasDuplicateUnresolvedMembers(connection)) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_request_one_unresolved_per_member
                    ON consultation_requests (member_id)
                    WHERE status IN (
                        'PENDING_REVIEW', 'NEED_MORE_INFO', 'WAITING_ACCEPTANCE', 'WAITING_PAYMENT'
                    )
                    """);
        }
    }

    private boolean hasDuplicateUnresolvedMembers(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT 1
                     FROM consultation_requests
                     WHERE status IN (
                         'PENDING_REVIEW', 'NEED_MORE_INFO', 'WAITING_ACCEPTANCE', 'WAITING_PAYMENT'
                     )
                     GROUP BY member_id
                     HAVING COUNT(*) > 1
                     LIMIT 1
                     """)) {
            return result.next();
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        if (!columnExists(connection, REQUESTS, column))
            execute(connection, "ALTER TABLE %s ADD COLUMN %s %s".formatted(REQUESTS, column, definition));
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (result.next())
                return true;
        }
        try (ResultSet result = metadata.getTables(
                connection.getCatalog(), null, table.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            if (result.next())
                return true;
        }
        try (ResultSet result = metadata.getColumns(
                connection.getCatalog(), null, table.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT))) {
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
