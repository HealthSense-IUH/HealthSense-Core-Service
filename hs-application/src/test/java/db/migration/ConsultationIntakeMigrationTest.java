package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class ConsultationIntakeMigrationTest {

    @Test
    void legacyRequestMigratesKnownIntakeReferencesAndMoreInfoWithoutFabrication() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:intake_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_requests (
                        id BIGINT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        health_record_id BIGINT,
                        reason VARCHAR(1000) NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        more_info_reason VARCHAR(500),
                        member_additional_note VARCHAR(1000),
                        reviewed_by_admin_id BIGINT,
                        reviewed_at TIMESTAMP WITH TIME ZONE,
                        doctor_reserved_at TIMESTAMP WITH TIME ZONE,
                        created_at TIMESTAMP WITH TIME ZONE,
                        updated_at TIMESTAMP WITH TIME ZONE
                    )
                    """);
            statement.execute("""
                    INSERT INTO consultation_requests (
                        id, member_id, reason, status, reviewed_at, doctor_reserved_at
                    ) VALUES (
                        21, 2, 'Legacy assigned request', 'WAITING_PAYMENT',
                        TIMESTAMP '2026-01-03 09:00:00', TIMESTAMP '2026-01-03 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO consultation_requests (
                        id, member_id, health_record_id, reason, status, more_info_reason,
                        member_additional_note, reviewed_by_admin_id, reviewed_at,
                        created_at, updated_at
                    ) VALUES (
                        20, 1, 99, 'Need long-term monitoring', 'PENDING_REVIEW',
                        'Please add symptom timing', 'Usually happens at night', 7,
                        TIMESTAMP '2026-01-02 10:00:00', TIMESTAMP '2026-01-01 09:00:00',
                        TIMESTAMP '2026-01-02 11:00:00'
                    )
                    """);
            V2__consultation_intake_request_state migration = new V2__consultation_intake_request_state();

            migration.migrate(context(connection));
            migration.migrate(context(connection));

            ResultSet intake = statement.executeQuery("""
                    SELECT reason_for_care, current_concern, member_note, status
                    FROM consultation_requests WHERE id = 20
                    """);
            assertTrue(intake.next());
            assertEquals("Need long-term monitoring", intake.getString("reason_for_care"));
            assertNull(intake.getString("current_concern"));
            assertEquals("Usually happens at night", intake.getString("member_note"));
            assertEquals("PENDING_REVIEW", intake.getString("status"));

            assertEquals(1, singleInt(statement, """
                    SELECT COUNT(*) FROM consultation_request_health_records
                    WHERE request_id = 20 AND health_record_id = 99
                    """));
            ResultSet history = statement.executeQuery("""
                    SELECT coordinator_message, requested_by, member_response, responded_at
                    FROM consultation_more_info_cycles WHERE request_id = 20
                    """);
            assertTrue(history.next());
            assertEquals("Please add symptom timing", history.getString("coordinator_message"));
            assertEquals(7L, history.getLong("requested_by"));
            assertEquals("Usually happens at night", history.getString("member_response"));
            assertNotNull(history.getTimestamp("responded_at"));
            assertFalse(history.next());
            assertNotNull(singleTimestamp(statement, """
                    SELECT intake_frozen_at FROM consultation_requests WHERE id = 21
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

    private int singleInt(Statement statement, String sql) throws Exception {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getInt(1);
    }

    private java.sql.Timestamp singleTimestamp(Statement statement, String sql) throws Exception {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getTimestamp(1);
    }
}
