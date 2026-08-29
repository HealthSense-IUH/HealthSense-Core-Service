package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class CancellationRefundClosureMigrationTest {
    @Test
    void createsSeparateRefundAndDurableReconciliationSchema() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:cancellation_refund;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_payments (
                        id BIGINT PRIMARY KEY, provider VARCHAR(30), status VARCHAR(30), updated_at TIMESTAMP WITH TIME ZONE)
                    """);
            statement.execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY, activated_at TIMESTAMP WITH TIME ZONE, status VARCHAR(30))
                    """);
            statement.execute("INSERT INTO consultation_payments (id, provider, status) VALUES (1, 'PAYOS', 'PAID')");
            statement.execute("INSERT INTO consultation_sessions (id, activated_at, status) VALUES (2, CURRENT_TIMESTAMP, 'ACTIVE')");

            V8__cancellation_refund_closure migration = new V8__cancellation_refund_closure();
            migration.migrate(context(connection));
            migration.migrate(context(connection));

            assertEquals("NOT_REQUESTED", value(statement,
                    "SELECT provider_cancellation_status FROM consultation_payments WHERE id = 1"));
            assertEquals(Boolean.TRUE, value(statement,
                    "SELECT meaningful_care_occurred FROM consultation_sessions WHERE id = 2"));
            assertEquals(1, count(statement,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'consultation_refunds'"));
            statement.execute("""
                    INSERT INTO consultation_refunds (
                        id, payment_id, agreement_id, member_id, original_paid_amount, currency,
                        refund_policy_reference, status, provider)
                    VALUES (10, 1, 20, 30, 100000, 'VND', 'policy-v3', 'REVIEW_REQUIRED', 'PAYOS')
                    """);
            assertEquals("PAID", value(statement, "SELECT status FROM consultation_payments WHERE id = 1"));
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
