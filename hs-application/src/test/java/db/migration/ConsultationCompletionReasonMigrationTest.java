package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultationCompletionReasonMigrationTest {

    @Test
    void replacesStaleCheckAndAllowsQueueContinuationCompletionReasons() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:completion_reasons;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")) {
            connection.createStatement().execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY,
                        completion_reason VARCHAR(40),
                        CONSTRAINT consultation_sessions_completion_reason_check
                            CHECK (completion_reason IS NULL OR completion_reason IN (
                                'PERIOD_ENDED', 'NORMAL_COMPLETION', 'ADMINISTRATIVE_CANCELLATION'
                            ))
                    )
                    """);

            assertThrows(SQLException.class, () -> insert(connection, 1, "CONTINUATION_STOPPED"));

            new V14__expand_consultation_completion_reasons().migrate(context(connection));

            assertDoesNotThrow(() -> insert(connection, 2, "CONTINUATION_STOPPED"));
            assertDoesNotThrow(() -> insert(connection, 3, "CONTINUATION_GRACE_EXPIRED"));
            assertThrows(SQLException.class, () -> insert(connection, 4, "UNKNOWN_REASON"));
        }
    }

    private void insert(Connection connection, long id, String reason) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO consultation_sessions (id, completion_reason) VALUES (?, ?)")) {
            statement.setLong(1, id);
            statement.setString(2, reason);
            statement.executeUpdate();
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override public org.flywaydb.core.api.configuration.Configuration getConfiguration() { return null; }
            @Override public Connection getConnection() { return connection; }
        };
    }
}
