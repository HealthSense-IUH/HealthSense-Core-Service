package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class EpisodeHealthRecordAuthorizationMigrationTest {

    @Test
    void backfillsOnlyExplicitRecordsForActuallyActivatedLegacyEpisodes() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:episode_hr_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY, member_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL,
                        request_id BIGINT, health_record_id BIGINT, status VARCHAR(30) NOT NULL,
                        started_at TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE consultation_request_health_records (
                        request_id BIGINT NOT NULL, selection_order INTEGER NOT NULL,
                        health_record_id BIGINT NOT NULL,
                        PRIMARY KEY (request_id, selection_order)
                    )
                    """);
            statement.execute("""
                    INSERT INTO consultation_sessions
                        (id, member_id, doctor_id, request_id, health_record_id, status, started_at, created_at)
                    VALUES
                        (10, 1, 101, 20, 501, 'ACTIVE', TIMESTAMP '2026-01-02 10:00:00', TIMESTAMP '2026-01-01 10:00:00'),
                        (11, 1, 102, 21, 504, 'SCHEDULED', NULL, TIMESTAMP '2026-01-03 10:00:00'),
                        (12, 1, 103, 22, 505, 'CANCELLED', NULL, TIMESTAMP '2026-01-04 10:00:00')
                    """);
            statement.execute("""
                    INSERT INTO consultation_request_health_records
                        (request_id, selection_order, health_record_id)
                    VALUES (20, 0, 501), (20, 1, 502), (21, 0, 504), (22, 0, 505)
                    """);

            V5__episode_health_record_authorization migration =
                    new V5__episode_health_record_authorization();
            migration.migrate(context(connection));
            migration.migrate(context(connection));

            assertNotNull(singleTimestamp(statement,
                    "SELECT activated_at FROM consultation_sessions WHERE id = 10"));
            assertNull(singleTimestamp(statement,
                    "SELECT activated_at FROM consultation_sessions WHERE id = 11"));
            assertNull(singleTimestamp(statement,
                    "SELECT activated_at FROM consultation_sessions WHERE id = 12"));
            assertEquals(2, singleInt(statement,
                    "SELECT COUNT(*) FROM consultation_episode_health_records WHERE session_id = 10"));
            assertEquals(0, singleInt(statement,
                    "SELECT COUNT(*) FROM consultation_episode_health_records WHERE session_id IN (11, 12)"));
            assertEquals(0, singleInt(statement,
                    "SELECT COUNT(*) FROM consultation_episode_health_records WHERE health_record_id = 503"));
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override public Configuration getConfiguration() { return null; }
            @Override public Connection getConnection() { return connection; }
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
