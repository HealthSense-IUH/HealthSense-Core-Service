package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AtomicDoctorReservationMigrationTest {

    @Test
    void currentLegacyAssignmentIsBackfilledOnceWithKnownAuditData() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:reservation_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_requests (
                        id BIGINT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        package_id BIGINT,
                        package_version INTEGER,
                        status VARCHAR(30) NOT NULL,
                        assigned_doctor_id BIGINT,
                        doctor_reserved_at TIMESTAMP WITH TIME ZONE,
                        payment_deadline TIMESTAMP WITH TIME ZONE,
                        reviewed_by_admin_id BIGINT
                    )
                    """);
            statement.execute("""
                    INSERT INTO consultation_requests VALUES (
                        100, 1, 10, 3, 'WAITING_PAYMENT', 2,
                        TIMESTAMP '2026-08-26 09:00:00',
                        TIMESTAMP '2026-08-26 09:30:00', 9
                    )
                    """);
            V3__atomic_doctor_reservations migration = new V3__atomic_doctor_reservations();

            migration.migrate(context(connection));
            migration.migrate(context(connection));

            ResultSet result = statement.executeQuery("""
                    SELECT request_id, doctor_id, package_id, package_version, reserved_by,
                           status, reserved_at, expires_at
                    FROM doctor_reservations WHERE request_id = 100
                    """);
            assertTrue(result.next());
            assertEquals(2L, result.getLong("doctor_id"));
            assertEquals(10L, result.getLong("package_id"));
            assertEquals(3, result.getInt("package_version"));
            assertEquals(9L, result.getLong("reserved_by"));
            assertEquals("ACTIVE", result.getString("status"));
            assertNotNull(result.getTimestamp("reserved_at"));
            assertNotNull(result.getTimestamp("expires_at"));
            assertFalse(result.next());
        }
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
}
