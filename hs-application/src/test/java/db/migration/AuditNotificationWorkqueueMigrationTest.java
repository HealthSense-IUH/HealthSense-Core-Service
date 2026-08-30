package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class AuditNotificationWorkqueueMigrationTest {
    @Test
    void createsAppendOnlyAuditNotificationAndDurableWorkSchemaWithoutFabricatingHistory() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:operations_v9;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
             Statement statement = connection.createStatement()) {
            V9__audit_notification_workqueue migration = new V9__audit_notification_workqueue();
            migration.migrate(context(connection));
            migration.migrate(context(connection));

            assertTable(statement, "business_audit_events");
            assertTable(statement, "user_notifications");
            assertTable(statement, "needs_action_items");
            assertTable(statement, "notification_projection_tasks");
            assertEquals(0, count(statement, "SELECT COUNT(*) FROM business_audit_events"));

            statement.execute("""
                    INSERT INTO business_audit_events (id, domain_type, domain_id, event_type, actor_type,
                        idempotency_key, occurred_at, created_at)
                    VALUES (1, 'REQUEST', 10, 'LEGACY_STATE_IMPORTED', 'SYSTEM', 'legacy:request:10',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """);
            assertThrows(SQLException.class, () -> statement.execute("""
                    INSERT INTO business_audit_events (id, domain_type, domain_id, event_type, actor_type,
                        idempotency_key, occurred_at, created_at)
                    VALUES (2, 'REQUEST', 10, 'LEGACY_STATE_IMPORTED', 'SYSTEM', 'legacy:request:10',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """));
        }
    }

    private void assertTable(Statement statement, String name) throws Exception {
        assertEquals(1, count(statement,
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + name + "'"));
    }

    private int count(Statement statement, String sql) throws Exception {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override public Configuration getConfiguration() { return null; }
            @Override public Connection getConnection() { return connection; }
        };
    }
}
