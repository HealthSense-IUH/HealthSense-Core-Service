package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Keeps legacy lifecycle workers from completing Queue Dispatch sessions at the
 * end of a consultation block. NOT VALID is intentional on PostgreSQL because
 * historical rows may already contain the invalid legacy completion reason;
 * PostgreSQL still enforces the constraint for all subsequent writes.
 */
public class V12__queue_dispatch_legacy_completion_fence extends BaseJavaMigration {

    private static final String TABLE = "consultation_sessions";
    private static final String CONSTRAINT = "ck_queue_dispatch_no_period_ended";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, TABLE) || constraintExists(connection, TABLE, CONSTRAINT)) return;

        boolean postgres = connection.getMetaData().getDatabaseProductName()
                .toLowerCase(java.util.Locale.ROOT).contains("postgresql");
        String notValid = postgres ? " NOT VALID" : "";
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    ALTER TABLE consultation_sessions
                    ADD CONSTRAINT ck_queue_dispatch_no_period_ended
                    CHECK (
                        flow_type <> 'QUEUE_DISPATCH_V1'
                        OR completion_reason IS NULL
                        OR completion_reason <> 'PERIOD_ENDED'
                    )%s
                    """.formatted(notValid));
        }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rows = metadata.getTables(null, null, table, new String[]{"TABLE"})) {
            if (rows.next()) return true;
        }
        try (ResultSet rows = metadata.getTables(null, null, table.toUpperCase(), new String[]{"TABLE"})) {
            return rows.next();
        }
    }

    private boolean constraintExists(Connection connection, String table, String constraint) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE LOWER(table_name) = LOWER(?) AND LOWER(constraint_name) = LOWER(?)
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, constraint);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        }
    }
}
