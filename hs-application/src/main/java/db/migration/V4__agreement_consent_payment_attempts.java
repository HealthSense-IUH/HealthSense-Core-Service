package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.Locale;

public class V4__agreement_consent_payment_attempts extends BaseJavaMigration {

    static final String REQUESTS = "consultation_requests";
    static final String PAYMENTS = "consultation_payments";
    static final String AGREEMENTS = "care_service_agreements";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, REQUESTS))
            return;

        createAgreementTables(connection);
        migrateLegacyOffers(connection);
        migratePaymentAttempts(connection);
        addExceptionalOverrideColumns(connection);
        addIntegrityIndexes(connection);
    }

    private void createAgreementTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS care_service_agreements (
                    id BIGINT NOT NULL,
                    request_id BIGINT NOT NULL,
                    member_id BIGINT NOT NULL,
                    doctor_id BIGINT NOT NULL,
                    package_id BIGINT,
                    package_family_id BIGINT,
                    package_code VARCHAR(80),
                    package_name VARCHAR(160),
                    package_version INTEGER,
                    service_description VARCHAR(4000),
                    price_amount DECIMAL(14,2),
                    currency VARCHAR(3),
                    duration_days INTEGER,
                    start_rule VARCHAR(60),
                    support_schedule_snapshot_json TEXT,
                    support_timezone_snapshot VARCHAR(80),
                    support_policy VARCHAR(80),
                    renewable BOOLEAN,
                    terms_policy_reference VARCHAR(255),
                    cancellation_policy_reference VARCHAR(255),
                    refund_policy_reference VARCHAR(255),
                    emergency_limitation VARCHAR(1000),
                    ai_limitation VARCHAR(1000),
                    service_limitation VARCHAR(2000),
                    health_data_scope_disclosure VARCHAR(2000),
                    status VARCHAR(30) NOT NULL,
                    accepted_by_member BIGINT,
                    accepted_at TIMESTAMP WITH TIME ZONE,
                    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
                    invalidated_at TIMESTAMP WITH TIME ZONE,
                    invalidation_reason VARCHAR(500),
                    consumed_at TIMESTAMP WITH TIME ZONE,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_care_service_agreements PRIMARY KEY (id),
                    CONSTRAINT fk_agreement_request FOREIGN KEY (request_id)
                        REFERENCES consultation_requests (id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS agreement_included_services (
                    agreement_id BIGINT NOT NULL,
                    service_order INTEGER NOT NULL,
                    service_code VARCHAR(80) NOT NULL,
                    CONSTRAINT pk_agreement_included_services PRIMARY KEY (agreement_id, service_order),
                    CONSTRAINT fk_agreement_included_services FOREIGN KEY (agreement_id)
                        REFERENCES care_service_agreements (id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS agreement_excluded_services (
                    agreement_id BIGINT NOT NULL,
                    service_order INTEGER NOT NULL,
                    service_code VARCHAR(80) NOT NULL,
                    CONSTRAINT pk_agreement_excluded_services PRIMARY KEY (agreement_id, service_order),
                    CONSTRAINT fk_agreement_excluded_services FOREIGN KEY (agreement_id)
                        REFERENCES care_service_agreements (id)
                )
                """);
    }

    private void migrateLegacyOffers(Connection connection) throws SQLException {
        if (!columnExists(connection, REQUESTS, "assigned_doctor_id")
                || !columnExists(connection, REQUESTS, "payment_deadline"))
            return;

        boolean hasPayments = tableExists(connection, PAYMENTS);
        String idExpression = hasPayments ? "COALESCE(p.id, r.id)" : "r.id";
        String paymentJoin = hasPayments ? "LEFT JOIN consultation_payments p ON p.request_id = r.id" : "";
        String packageJoin = tableExists(connection, "care_service_packages")
                ? "LEFT JOIN care_service_packages pkg ON pkg.id = r.package_id" : "";
        String profileJoin = tableExists(connection, "doctor_care_profiles")
                ? "LEFT JOIN doctor_care_profiles profile ON profile.doctor_id = r.assigned_doctor_id" : "";
        String packageValue = tableExists(connection, "care_service_packages") ? "pkg.%s" : "NULL";
        String profileValue = tableExists(connection, "doctor_care_profiles") ? "profile.%s" : "NULL";

        execute(connection, """
                INSERT INTO care_service_agreements (
                    id, request_id, member_id, doctor_id, package_id, package_family_id,
                    package_code, package_name, package_version, service_description,
                    price_amount, currency, duration_days, start_rule,
                    support_schedule_snapshot_json, support_timezone_snapshot, support_policy,
                    renewable, terms_policy_reference, cancellation_policy_reference,
                    refund_policy_reference, emergency_limitation, ai_limitation,
                    service_limitation, health_data_scope_disclosure, status, valid_until,
                    consumed_at, version, created_at, updated_at
                )
                SELECT %s, r.id, r.member_id, r.assigned_doctor_id, r.package_id,
                       %s, %s, %s, r.package_version, %s,
                       r.package_price_snapshot, COALESCE(%s, 'VND'),
                       r.package_duration_days_snapshot, 'IMMEDIATE_AFTER_VERIFIED_PAYMENT',
                       %s, %s, COALESCE(%s, 'ASSIGNED_DOCTOR_SUPPORT_SCHEDULE'),
                       COALESCE(%s, FALSE), COALESCE(%s, 'HEALTHSENSE_TERMS_V1'),
                       'HEALTHSENSE_CANCELLATION_POLICY_V1', 'HEALTHSENSE_REFUND_POLICY_V1',
                       'This service is not an emergency service and does not guarantee immediate response.',
                       'AI outputs are decision support only and do not replace professional clinical judgment.',
                       'Care is limited to the agreed package scope and assigned Doctor support schedule.',
                       'Only health data explicitly selected or shared for this care episode is authorized.',
                       CASE WHEN r.status = 'FULFILLED' THEN 'CONSUMED' ELSE 'PENDING_ACCEPTANCE' END,
                       COALESCE(r.payment_deadline, CURRENT_TIMESTAMP),
                       CASE WHEN r.status = 'FULFILLED' THEN COALESCE(r.updated_at, CURRENT_TIMESTAMP) ELSE NULL END,
                       0, COALESCE(r.doctor_reserved_at, CURRENT_TIMESTAMP),
                       COALESCE(r.updated_at, r.doctor_reserved_at, CURRENT_TIMESTAMP)
                FROM consultation_requests r
                %s
                %s
                %s
                WHERE r.assigned_doctor_id IS NOT NULL
                  AND r.status IN ('WAITING_ACCEPTANCE', 'WAITING_PAYMENT', 'FULFILLED')
                  AND NOT EXISTS (
                      SELECT 1 FROM care_service_agreements agreement WHERE agreement.request_id = r.id
                  )
                """.formatted(
                idExpression,
                packageValue.formatted("family_id"),
                packageValue.formatted("code"),
                packageValue.formatted("name"),
                packageValue.formatted("description"),
                packageValue.formatted("currency"),
                profileValue.formatted("availability_json"),
                profileValue.formatted("timezone"),
                packageValue.formatted("support_policy"),
                packageValue.formatted("renewable"),
                packageValue.formatted("terms_policy_reference"),
                paymentJoin,
                packageJoin,
                profileJoin
        ));

        execute(connection, """
                UPDATE consultation_requests
                SET status = 'WAITING_ACCEPTANCE'
                WHERE status = 'WAITING_PAYMENT'
                  AND EXISTS (
                      SELECT 1 FROM care_service_agreements agreement
                      WHERE agreement.request_id = consultation_requests.id
                        AND agreement.accepted_at IS NULL
                  )
                """);
    }

    private void migratePaymentAttempts(Connection connection) throws SQLException {
        if (!tableExists(connection, PAYMENTS))
            return;
        addColumnIfMissing(connection, PAYMENTS, "agreement_id", "BIGINT");
        addColumnIfMissing(connection, PAYMENTS, "attempt_number", "INTEGER");
        execute(connection, """
                UPDATE consultation_payments payment
                SET agreement_id = (
                    SELECT agreement.id FROM care_service_agreements agreement
                    WHERE agreement.request_id = payment.request_id
                )
                WHERE payment.agreement_id IS NULL
                """);
        execute(connection, "UPDATE consultation_payments SET attempt_number = 1 WHERE attempt_number IS NULL");
        execute(connection, "ALTER TABLE consultation_payments DROP CONSTRAINT IF EXISTS uq_payment_request");
        if (isPostgreSql(connection))
            execute(connection, "ALTER TABLE consultation_payments DROP CONSTRAINT IF EXISTS consultation_payments_request_id_key");
        execute(connection, """
                CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_agreement_attempt
                ON consultation_payments (agreement_id, attempt_number)
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_payment_agreement_status
                ON consultation_payments (agreement_id, status)
                """);
    }

    private void addExceptionalOverrideColumns(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_sessions"))
            return;
        addColumnIfMissing(connection, "consultation_sessions", "exceptional_override", "BOOLEAN DEFAULT FALSE NOT NULL");
        addColumnIfMissing(connection, "consultation_sessions", "override_reason", "VARCHAR(1000)");
    }

    private void addIntegrityIndexes(Connection connection) throws SQLException {
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_agreement_request_status ON care_service_agreements (request_id, status)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_agreement_member_status ON care_service_agreements (member_id, status)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_agreement_valid_until ON care_service_agreements (status, valid_until)");
        if (isPostgreSql(connection)) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_agreement_one_current_per_request
                    ON care_service_agreements (request_id)
                    WHERE status IN ('DRAFT', 'PENDING_ACCEPTANCE', 'ACCEPTED')
                    """);
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_one_paid_per_agreement
                    ON consultation_payments (agreement_id)
                    WHERE status = 'PAID'
                    """);
        }
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String definition) throws SQLException {
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

    private boolean isPostgreSql(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgresql");
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
