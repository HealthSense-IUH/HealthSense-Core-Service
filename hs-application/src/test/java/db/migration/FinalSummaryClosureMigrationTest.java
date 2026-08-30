package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class FinalSummaryClosureMigrationTest {

    @Test
    void backfillsReconstructableClosureStateAndCreatesImmutableHistoryTables() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:summary_closure_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY, member_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL, ends_at TIMESTAMP WITH TIME ZONE,
                        completed_at TIMESTAMP WITH TIME ZONE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE consultation_final_summaries (
                        id BIGINT PRIMARY KEY, session_id BIGINT NOT NULL, status VARCHAR(30) NOT NULL,
                        summary TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE user_accounts (
                        id BIGINT PRIMARY KEY, role VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO user_accounts (id, role, status)
                    VALUES (10, 'DOCTOR', 'ACTIVE'), (11, 'DOCTOR', 'INACTIVE')
                    """);
            statement.execute("""
                    INSERT INTO consultation_sessions (id, member_id, doctor_id, status, ends_at, completed_at)
                    VALUES
                        (100, 1, 10, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                        (101, 1, 10, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                        (102, 1, 11, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                        (103, 1, 10, 'ACTIVE', CURRENT_TIMESTAMP, NULL)
                    """);
            statement.execute("""
                    INSERT INTO consultation_final_summaries (id, session_id, status, summary)
                    VALUES (200, 100, 'FINALIZED', 'done'), (201, 101, 'DRAFT', 'draft')
                    """);

            V6__final_summary_closure migration = new V6__final_summary_closure();
            migration.migrate(context(connection));
            migration.migrate(context(connection));
            new V6_1__final_summary_draft_compatibility().migrate(context(connection));

            assertEquals("SUMMARY_FINALIZED", value(statement,
                    "SELECT summary_closure_status FROM consultation_sessions WHERE id = 100"));
            assertEquals("SUMMARY_PENDING", value(statement,
                    "SELECT summary_closure_status FROM consultation_sessions WHERE id = 101"));
            assertEquals("ESCALATED", value(statement,
                    "SELECT summary_closure_status FROM consultation_sessions WHERE id = 102"));
            assertNull(value(statement,
                    "SELECT summary_closure_status FROM consultation_sessions WHERE id = 103"));
            assertNotNull(value(statement,
                    "SELECT summary_due_at FROM consultation_sessions WHERE id = 101"));
            assertEquals(1, count(statement,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'consultation_final_summary_addenda'"));
            assertEquals(1, count(statement,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'consultation_final_summary_health_records'"));
            statement.execute("""
                    INSERT INTO consultation_final_summaries (id, session_id, status, summary, version)
                    VALUES (202, 102, 'DRAFT', NULL, 0)
                    """);
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
