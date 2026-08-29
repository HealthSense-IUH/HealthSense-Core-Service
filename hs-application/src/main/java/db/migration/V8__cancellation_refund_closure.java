package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.Locale;

public class V8__cancellation_refund_closure extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        extendPayments(connection);
        extendSessions(connection);
        createRefunds(connection);
    }

    private void extendPayments(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_payments")) return;
        addColumnIfMissing(connection, "consultation_payments", "provider_cancellation_status", "VARCHAR(30)");
        addColumnIfMissing(connection, "consultation_payments", "provider_cancellation_requested_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_payments", "provider_cancellation_completed_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_payments", "provider_cancellation_last_attempt_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_payments", "provider_cancellation_error", "VARCHAR(1000)");
        execute(connection, "UPDATE consultation_payments SET provider_cancellation_status = 'NOT_REQUESTED' WHERE provider_cancellation_status IS NULL");
        execute(connection, "ALTER TABLE consultation_payments ALTER COLUMN provider_cancellation_status SET NOT NULL");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_payment_provider_cancellation ON consultation_payments (provider_cancellation_status, provider_cancellation_last_attempt_at)");
    }

    private void extendSessions(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_sessions")) return;
        addColumnIfMissing(connection, "consultation_sessions", "override_service_scope", "VARCHAR(2000)");
        addColumnIfMissing(connection, "consultation_sessions", "termination_reason", "VARCHAR(50)");
        addColumnIfMissing(connection, "consultation_sessions", "termination_requested_by", "BIGINT");
        addColumnIfMissing(connection, "consultation_sessions", "termination_requested_by_role", "VARCHAR(30)");
        addColumnIfMissing(connection, "consultation_sessions", "termination_requested_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_sessions", "termination_decided_by", "BIGINT");
        addColumnIfMissing(connection, "consultation_sessions", "termination_decided_by_role", "VARCHAR(30)");
        addColumnIfMissing(connection, "consultation_sessions", "termination_decided_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "consultation_sessions", "meaningful_care_occurred", "BOOLEAN");
        addColumnIfMissing(connection, "consultation_sessions", "operational_review_required", "BOOLEAN");
        addColumnIfMissing(connection, "consultation_sessions", "operational_review_reason", "VARCHAR(50)");
        addColumnIfMissing(connection, "consultation_sessions", "operational_review_flagged_at", "TIMESTAMP WITH TIME ZONE");
        execute(connection, "UPDATE consultation_sessions SET meaningful_care_occurred = (activated_at IS NOT NULL) WHERE meaningful_care_occurred IS NULL");
        execute(connection, "UPDATE consultation_sessions SET operational_review_required = FALSE WHERE operational_review_required IS NULL");
        execute(connection, "ALTER TABLE consultation_sessions ALTER COLUMN meaningful_care_occurred SET NOT NULL");
        execute(connection, "ALTER TABLE consultation_sessions ALTER COLUMN operational_review_required SET NOT NULL");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_session_operational_review ON consultation_sessions (operational_review_required, operational_review_flagged_at)");
    }

    private void createRefunds(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_payments")) return;
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_refunds (
                    id BIGINT NOT NULL,
                    payment_id BIGINT NOT NULL,
                    request_id BIGINT,
                    renewal_id BIGINT,
                    session_id BIGINT,
                    agreement_id BIGINT NOT NULL,
                    member_id BIGINT NOT NULL,
                    original_paid_amount DECIMAL(14,2) NOT NULL,
                    currency VARCHAR(3) NOT NULL,
                    refund_policy_reference VARCHAR(255) NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    recommendation VARCHAR(20),
                    recommended_amount DECIMAL(14,2),
                    review_reason VARCHAR(1000),
                    operational_context VARCHAR(4000),
                    reviewed_by BIGINT,
                    reviewed_at TIMESTAMP WITH TIME ZONE,
                    approved_amount DECIMAL(14,2),
                    decision_reason VARCHAR(1000),
                    decided_by BIGINT,
                    decided_at TIMESTAMP WITH TIME ZONE,
                    provider VARCHAR(30) NOT NULL,
                    provider_refund_id VARCHAR(150),
                    provider_result VARCHAR(1000),
                    execution_attempts INTEGER NOT NULL DEFAULT 0,
                    last_execution_at TIMESTAMP WITH TIME ZONE,
                    completed_at TIMESTAMP WITH TIME ZONE,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_refunds PRIMARY KEY (id),
                    CONSTRAINT uq_refund_payment UNIQUE (payment_id),
                    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id)
                        REFERENCES consultation_payments (id)
                )
                """);
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_refund_status_updated ON consultation_refunds (status, updated_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_refund_member_created ON consultation_refunds (member_id, created_at)");
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String definition)
            throws SQLException {
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
