package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.Locale;

public class V7__renewal_session_extensions extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, "consultation_sessions")) return;

        createRenewals(connection);
        extendAgreements(connection);
        extendPayments(connection);
        createExtensions(connection);
        createIndexes(connection);
    }

    private void createRenewals(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_renewals (
                    id BIGINT NOT NULL,
                    session_id BIGINT NOT NULL,
                    member_id BIGINT NOT NULL,
                    doctor_id BIGINT NOT NULL,
                    package_family_id BIGINT NOT NULL,
                    package_id BIGINT,
                    package_version INTEGER,
                    duration_days INTEGER,
                    price_amount DECIMAL(14,2),
                    currency VARCHAR(3),
                    support_schedule_snapshot_json TEXT,
                    support_timezone_snapshot VARCHAR(80),
                    previous_ends_at TIMESTAMP WITH TIME ZONE,
                    proposed_new_ends_at TIMESTAMP WITH TIME ZONE,
                    agreement_id BIGINT,
                    successful_payment_id BIGINT,
                    status VARCHAR(40) NOT NULL,
                    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    reviewed_by BIGINT,
                    review_started_at TIMESTAMP WITH TIME ZONE,
                    reviewed_at TIMESTAMP WITH TIME ZONE,
                    rejection_reason VARCHAR(1000),
                    payment_deadline TIMESTAMP WITH TIME ZONE,
                    applied_at TIMESTAMP WITH TIME ZONE,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_renewals PRIMARY KEY (id),
                    CONSTRAINT fk_renewal_session FOREIGN KEY (session_id)
                        REFERENCES consultation_sessions (id)
                )
                """);
    }

    private void extendAgreements(Connection connection) throws SQLException {
        if (!tableExists(connection, "care_service_agreements")) return;
        addColumnIfMissing(connection, "care_service_agreements", "renewal_id", "BIGINT");
        addColumnIfMissing(connection, "care_service_agreements", "agreement_type", "VARCHAR(30)");
        addColumnIfMissing(connection, "care_service_agreements", "extension_starts_at", "TIMESTAMP WITH TIME ZONE");
        addColumnIfMissing(connection, "care_service_agreements", "resulting_ends_at", "TIMESTAMP WITH TIME ZONE");
        execute(connection, "UPDATE care_service_agreements SET agreement_type = 'INITIAL_CARE' WHERE agreement_type IS NULL");
        execute(connection, "ALTER TABLE care_service_agreements ALTER COLUMN agreement_type SET NOT NULL");
        execute(connection, "ALTER TABLE care_service_agreements ALTER COLUMN request_id DROP NOT NULL");
    }

    private void extendPayments(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_payments")) return;
        addColumnIfMissing(connection, "consultation_payments", "renewal_id", "BIGINT");
        addColumnIfMissing(connection, "consultation_payments", "payment_purpose", "VARCHAR(30)");
        execute(connection, "UPDATE consultation_payments SET payment_purpose = 'INITIAL_CARE' WHERE payment_purpose IS NULL");
        execute(connection, "ALTER TABLE consultation_payments ALTER COLUMN payment_purpose SET NOT NULL");
        execute(connection, "ALTER TABLE consultation_payments ALTER COLUMN request_id DROP NOT NULL");
    }

    private void createExtensions(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_session_extensions (
                    id BIGINT NOT NULL,
                    session_id BIGINT NOT NULL,
                    renewal_id BIGINT NOT NULL,
                    agreement_id BIGINT NOT NULL,
                    payment_id BIGINT NOT NULL,
                    previous_ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    new_ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    duration_days INTEGER NOT NULL,
                    package_id BIGINT NOT NULL,
                    package_version INTEGER NOT NULL,
                    price_amount DECIMAL(14,2) NOT NULL,
                    currency VARCHAR(3) NOT NULL,
                    support_schedule_snapshot_json TEXT NOT NULL,
                    support_timezone_snapshot VARCHAR(80) NOT NULL,
                    applied_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_session_extensions PRIMARY KEY (id),
                    CONSTRAINT uq_session_extension_renewal UNIQUE (renewal_id),
                    CONSTRAINT uq_session_extension_payment UNIQUE (payment_id),
                    CONSTRAINT fk_session_extension_session FOREIGN KEY (session_id)
                        REFERENCES consultation_sessions (id),
                    CONSTRAINT fk_session_extension_renewal FOREIGN KEY (renewal_id)
                        REFERENCES consultation_renewals (id)
                )
                """);
    }

    private void createIndexes(Connection connection) throws SQLException {
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_renewal_session_status ON consultation_renewals (session_id, status)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_renewal_doctor_window ON consultation_renewals (doctor_id, previous_ends_at, proposed_new_ends_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_renewal_member_created ON consultation_renewals (member_id, created_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_session_extension_session_applied ON consultation_session_extensions (session_id, applied_at)");
        if (connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgres")) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_unresolved_renewal_per_session
                    ON consultation_renewals (session_id)
                    WHERE status IN ('REQUESTED','UNDER_REVIEW','APPROVED','PENDING_ACCEPTANCE','WAITING_PAYMENT','PAID','REQUIRES_REVIEW')
                    """);
        }
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
