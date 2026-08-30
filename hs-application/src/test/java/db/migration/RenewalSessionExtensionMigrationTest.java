package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class RenewalSessionExtensionMigrationTest {

    @Test
    void preservesInitialCommerceAndCreatesRenewalExtensionHistorySchema() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:renewal_extension_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE consultation_requests (id BIGINT PRIMARY KEY)");
            statement.execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY, member_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL, ends_at TIMESTAMP WITH TIME ZONE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE care_service_agreements (
                        id BIGINT PRIMARY KEY, request_id BIGINT NOT NULL, member_id BIGINT NOT NULL,
                        doctor_id BIGINT NOT NULL, status VARCHAR(30) NOT NULL,
                        valid_until TIMESTAMP WITH TIME ZONE NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE consultation_payments (
                        id BIGINT PRIMARY KEY, request_id BIGINT NOT NULL, agreement_id BIGINT NOT NULL,
                        member_id BIGINT NOT NULL, provider VARCHAR(30) NOT NULL, order_code BIGINT NOT NULL,
                        amount DECIMAL(14,2) NOT NULL, currency VARCHAR(3) NOT NULL,
                        status VARCHAR(30) NOT NULL, expires_at TIMESTAMP WITH TIME ZONE NOT NULL
                    )
                    """);
            statement.execute("INSERT INTO consultation_requests VALUES (1)");
            statement.execute("INSERT INTO consultation_sessions VALUES (10, 1, 2, 'ACTIVE', CURRENT_TIMESTAMP)");
            statement.execute("INSERT INTO care_service_agreements VALUES (20, 1, 1, 2, 'CONSUMED', CURRENT_TIMESTAMP)");
            statement.execute("INSERT INTO consultation_payments VALUES (30, 1, 20, 1, 'PAYOS', 123, 100000, 'VND', 'PAID', CURRENT_TIMESTAMP)");

            V7__renewal_session_extensions migration = new V7__renewal_session_extensions();
            migration.migrate(context(connection));
            migration.migrate(context(connection));
            new V7_1__renewal_review_integrity().migrate(context(connection));

            assertEquals("INITIAL_CARE", value(statement,
                    "SELECT agreement_type FROM care_service_agreements WHERE id = 20"));
            assertEquals("INITIAL_CARE", value(statement,
                    "SELECT payment_purpose FROM consultation_payments WHERE id = 30"));
            assertEquals(1, count(statement,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'consultation_renewals'"));
            assertEquals(1, count(statement,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'consultation_session_extensions'"));

            statement.execute("""
                    INSERT INTO consultation_renewals (
                        id, session_id, member_id, doctor_id, package_family_id, status, requested_at, version)
                    VALUES (40, 10, 1, 2, 50, 'WAITING_PAYMENT', CURRENT_TIMESTAMP, 0)
                    """);
            statement.execute("""
                    INSERT INTO care_service_agreements (
                        id, request_id, renewal_id, agreement_type, member_id, doctor_id, status, valid_until)
                    VALUES (21, NULL, 40, 'RENEWAL', 1, 2, 'ACCEPTED', CURRENT_TIMESTAMP)
                    """);
            statement.execute("""
                    INSERT INTO consultation_payments (
                        id, request_id, renewal_id, payment_purpose, agreement_id, member_id, provider,
                        order_code, amount, currency, status, expires_at)
                    VALUES (31, NULL, 40, 'RENEWAL', 21, 1, 'PAYOS', 124, 150000, 'VND', 'PAID', CURRENT_TIMESTAMP)
                    """);
            statement.execute("""
                    INSERT INTO consultation_session_extensions (
                        id, session_id, renewal_id, agreement_id, payment_id, previous_ends_at, new_ends_at,
                        duration_days, package_id, package_version, price_amount, currency,
                        support_schedule_snapshot_json, support_timezone_snapshot, applied_at)
                    VALUES (50, 10, 40, 21, 31, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                        30, 101, 2, 150000, 'VND', '{}', 'Asia/Ho_Chi_Minh', CURRENT_TIMESTAMP)
                    """);
            assertEquals(1, count(statement,
                    "SELECT COUNT(*) FROM consultation_session_extensions WHERE session_id = 10"));
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override public Configuration getConfiguration() { return null; }
            @Override public Connection getConnection() { return connection; }
        };
    }

    private Object value(Statement statement, String sql) throws Exception {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getObject(1);
        }
    }

    private int count(Statement statement, String sql) throws Exception {
        return ((Number) value(statement, sql)).intValue();
    }
}
