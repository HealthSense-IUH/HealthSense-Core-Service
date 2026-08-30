package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AgreementConsentPaymentMigrationTest {

    @Test
    void legacyPaymentHistoryIsPreservedWithoutInventingAcceptance() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:agreement_payment_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ); Statement statement = connection.createStatement()) {
            createLegacySchema(statement);
            V4__agreement_consent_payment_attempts migration = new V4__agreement_consent_payment_attempts();

            migration.migrate(context(connection));
            migration.migrate(context(connection));

            ResultSet agreement = statement.executeQuery("""
                    SELECT id, status, accepted_at, package_code, price_amount, currency
                    FROM care_service_agreements WHERE request_id = 100
                    """);
            assertTrue(agreement.next());
            long agreementId = agreement.getLong("id");
            assertEquals("PENDING_ACCEPTANCE", agreement.getString("status"));
            assertNull(agreement.getTimestamp("accepted_at"));
            assertEquals("CARE_7D", agreement.getString("package_code"));
            assertEquals("100000.00", agreement.getBigDecimal("price_amount").toPlainString());
            assertEquals("VND", agreement.getString("currency"));
            assertFalse(agreement.next());

            assertEquals("WAITING_ACCEPTANCE", singleString(statement,
                    "SELECT status FROM consultation_requests WHERE id = 100"));
            ResultSet payment = statement.executeQuery("""
                    SELECT agreement_id, attempt_number, status
                    FROM consultation_payments WHERE id = 200
                    """);
            assertTrue(payment.next());
            assertEquals(agreementId, payment.getLong("agreement_id"));
            assertEquals(1, payment.getInt("attempt_number"));
            assertEquals("FAILED", payment.getString("status"));

            statement.execute("""
                    INSERT INTO consultation_payments
                        (id, request_id, agreement_id, attempt_number, member_id, provider,
                         order_code, amount, currency, status, expires_at)
                    VALUES
                        (201, 100, %d, 2, 10, 'PAYOS', 123457, 100000.00, 'VND',
                         'PENDING', CURRENT_TIMESTAMP)
                    """.formatted(agreementId));
            assertEquals(2, singleInt(statement,
                    "SELECT COUNT(*) FROM consultation_payments WHERE request_id = 100"));
            assertFalse(singleBoolean(statement,
                    "SELECT exceptional_override FROM consultation_sessions WHERE id = 300"));
        }
    }

    private void createLegacySchema(Statement statement) throws Exception {
        statement.execute("""
                CREATE TABLE care_service_packages (
                    id BIGINT PRIMARY KEY, family_id BIGINT, code VARCHAR(80), name VARCHAR(160),
                    version_number INTEGER, description VARCHAR(1000), currency VARCHAR(3),
                    support_policy VARCHAR(80), renewable BOOLEAN, terms_policy_reference VARCHAR(255)
                )
                """);
        statement.execute("""
                CREATE TABLE doctor_care_profiles (
                    doctor_id BIGINT PRIMARY KEY, availability_json TEXT, timezone VARCHAR(80)
                )
                """);
        statement.execute("""
                CREATE TABLE consultation_requests (
                    id BIGINT PRIMARY KEY, member_id BIGINT NOT NULL, package_id BIGINT,
                    package_version INTEGER, package_price_snapshot DECIMAL(14,2),
                    package_duration_days_snapshot INTEGER, status VARCHAR(30) NOT NULL,
                    assigned_doctor_id BIGINT, doctor_reserved_at TIMESTAMP WITH TIME ZONE,
                    payment_deadline TIMESTAMP WITH TIME ZONE, updated_at TIMESTAMP WITH TIME ZONE
                )
                """);
        statement.execute("""
                CREATE TABLE consultation_payments (
                    id BIGINT PRIMARY KEY, request_id BIGINT NOT NULL, member_id BIGINT NOT NULL,
                    provider VARCHAR(30) NOT NULL, order_code BIGINT NOT NULL,
                    payment_link_id VARCHAR(100), amount DECIMAL(14,2) NOT NULL,
                    currency VARCHAR(3) NOT NULL, status VARCHAR(30) NOT NULL,
                    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    CONSTRAINT uq_payment_request UNIQUE (request_id)
                )
                """);
        statement.execute("""
                CREATE TABLE consultation_sessions (id BIGINT PRIMARY KEY)
                """);
        statement.execute("""
                INSERT INTO care_service_packages VALUES
                    (30, 31, 'CARE_7D', 'Care 7 days', 3, 'Remote care', 'VND',
                     'ASSIGNED_DOCTOR_SUPPORT_SCHEDULE', TRUE, 'TERMS_V3')
                """);
        statement.execute("""
                INSERT INTO doctor_care_profiles VALUES
                    (40, '{"weekly":[]}', 'Asia/Ho_Chi_Minh')
                """);
        statement.execute("""
                INSERT INTO consultation_requests VALUES
                    (100, 10, 30, 3, 100000.00, 7, 'WAITING_PAYMENT', 40,
                     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """);
        statement.execute("""
                INSERT INTO consultation_payments VALUES
                    (200, 100, 10, 'PAYOS', 123456, 'legacy-link', 100000.00,
                     'VND', 'FAILED', CURRENT_TIMESTAMP)
                """);
        statement.execute("INSERT INTO consultation_sessions (id) VALUES (300)");
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override
            public Configuration getConfiguration() {
                return null;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }
        };
    }

    private String singleString(Statement statement, String sql) throws Exception {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getString(1);
    }

    private int singleInt(Statement statement, String sql) throws Exception {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getInt(1);
    }

    private boolean singleBoolean(Statement statement, String sql) throws Exception {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getBoolean(1);
    }
}
