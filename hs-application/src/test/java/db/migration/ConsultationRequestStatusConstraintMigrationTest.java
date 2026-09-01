package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConsultationRequestStatusConstraintMigrationTest {

    @Test
    void migrationAllowsAgreementAcceptanceWorkflowRequestStatuses() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:request_status_constraint;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_requests (
                        id BIGINT PRIMARY KEY,
                        status VARCHAR(30) NOT NULL,
                        CONSTRAINT consultation_requests_status_check
                            CHECK (status IN ('PENDING_REVIEW', 'WAITING_PAYMENT', 'FULFILLED', 'REJECTED', 'CANCELLED'))
                    )
                    """);
            statement.execute("INSERT INTO consultation_requests (id, status) VALUES (1, 'PENDING_REVIEW')");

            V10__consultation_request_status_constraint migration =
                    new V10__consultation_request_status_constraint();
            migration.migrate(context(connection));
            migration.migrate(context(connection));

            assertDoesNotThrow(() -> statement.execute("""
                    UPDATE consultation_requests
                    SET status = 'WAITING_ACCEPTANCE'
                    WHERE id = 1
                    """));
            assertDoesNotThrow(() -> statement.execute("""
                    UPDATE consultation_requests
                    SET status = 'NEED_MORE_INFO'
                    WHERE id = 1
                    """));
            assertDoesNotThrow(() -> statement.execute("""
                    UPDATE consultation_requests
                    SET status = 'EXPIRED'
                    WHERE id = 1
                    """));
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
