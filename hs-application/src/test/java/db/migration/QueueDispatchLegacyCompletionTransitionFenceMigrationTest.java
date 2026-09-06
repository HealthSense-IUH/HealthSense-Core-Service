package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueueDispatchLegacyCompletionTransitionFenceMigrationTest {

    @Test
    void removesV12RowStateConstraintSoHistoricalRowsCanBeUpdated() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:queue_transition_fence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")) {
            connection.createStatement().execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY,
                        flow_type VARCHAR(30) NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        completion_reason VARCHAR(40),
                        summary_due_at TIMESTAMP WITH TIME ZONE,
                        CONSTRAINT ck_queue_dispatch_no_period_ended CHECK (
                            flow_type <> 'QUEUE_DISPATCH_V1'
                            OR completion_reason IS NULL
                            OR completion_reason <> 'PERIOD_ENDED'
                        )
                    )
                    """);

            new V13__replace_queue_completion_check_with_transition_fence().migrate(context(connection));

            connection.createStatement().execute("""
                    INSERT INTO consultation_sessions (id, flow_type, status, completion_reason)
                    VALUES (1, 'QUEUE_DISPATCH_V1', 'COMPLETED', 'PERIOD_ENDED')
                    """);
            connection.createStatement().execute("""
                    UPDATE consultation_sessions
                    SET summary_due_at = CURRENT_TIMESTAMP
                    WHERE id = 1
                    """);
            try (var rows = connection.createStatement().executeQuery("""
                    SELECT COUNT(*) FROM information_schema.table_constraints
                    WHERE LOWER(constraint_name) = 'ck_queue_dispatch_no_period_ended'
                    """)) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override public org.flywaydb.core.api.configuration.Configuration getConfiguration() { return null; }
            @Override public Connection getConnection() { return connection; }
        };
    }
}
