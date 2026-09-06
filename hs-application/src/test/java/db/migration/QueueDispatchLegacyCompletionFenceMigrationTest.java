package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueueDispatchLegacyCompletionFenceMigrationTest {

    @Test
    void rejectsLegacyPeriodEndedForQueueButAllowsQueueContinuationAndLegacyCompletion() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:queue_legacy_completion_fence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")) {
            connection.createStatement().execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY,
                        flow_type VARCHAR(30) NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        completion_reason VARCHAR(40)
                    )
                    """);
            connection.createStatement().execute("""
                    INSERT INTO consultation_sessions (id, flow_type, status)
                    VALUES (1, 'QUEUE_DISPATCH_V1', 'ACTIVE'), (2, 'LEGACY_V3', 'ACTIVE')
                    """);

            new V12__queue_dispatch_legacy_completion_fence().migrate(context(connection));

            assertThrows(SQLException.class, () -> connection.createStatement().execute("""
                    UPDATE consultation_sessions
                    SET status = 'COMPLETED', completion_reason = 'PERIOD_ENDED'
                    WHERE id = 1
                    """));
            assertDoesNotThrow(() -> connection.createStatement().execute("""
                    UPDATE consultation_sessions
                    SET status = 'COMPLETED', completion_reason = 'CONTINUATION_GRACE_EXPIRED'
                    WHERE id = 1
                    """));
            assertDoesNotThrow(() -> connection.createStatement().execute("""
                    UPDATE consultation_sessions
                    SET status = 'COMPLETED', completion_reason = 'PERIOD_ENDED'
                    WHERE id = 2
                    """));
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override public org.flywaydb.core.api.configuration.Configuration getConfiguration() { return null; }
            @Override public Connection getConnection() { return connection; }
        };
    }
}
