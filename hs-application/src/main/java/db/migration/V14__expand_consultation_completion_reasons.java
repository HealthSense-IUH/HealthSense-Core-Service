package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces stale Hibernate-generated completion-reason checks with the Queue
 * Dispatch V1 completion reasons used by the continuation lifecycle.
 */
public class V14__expand_consultation_completion_reasons extends BaseJavaMigration {

    private static final String TABLE = "consultation_sessions";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, TABLE)) return;

        for (String constraint : completionReasonChecks(connection)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE consultation_sessions DROP CONSTRAINT "
                        + quoteIdentifier(connection, constraint));
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    ALTER TABLE consultation_sessions
                    ADD CONSTRAINT ck_consultation_session_completion_reason
                    CHECK (
                        completion_reason IS NULL OR completion_reason IN (
                            'PERIOD_ENDED',
                            'CONTINUATION_STOPPED',
                            'CONTINUATION_GRACE_EXPIRED',
                            'NORMAL_COMPLETION',
                            'ADMINISTRATIVE_CANCELLATION'
                        )
                    )
                    """);
        }
    }

    private List<String> completionReasonChecks(Connection connection) throws SQLException {
        String sql = """
                SELECT tc.constraint_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.check_constraints cc
                  ON cc.constraint_catalog = tc.constraint_catalog
                 AND cc.constraint_schema = tc.constraint_schema
                 AND cc.constraint_name = tc.constraint_name
                WHERE LOWER(tc.table_name) = LOWER(?)
                  AND UPPER(tc.constraint_type) = 'CHECK'
                  AND LOWER(cc.check_clause) LIKE '%completion_reason%'
                """;
        List<String> constraints = new ArrayList<>();
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, TABLE);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) constraints.add(rows.getString(1));
            }
        }
        return constraints;
    }

    private String quoteIdentifier(Connection connection, String identifier) throws SQLException {
        String quote = connection.getMetaData().getIdentifierQuoteString();
        if (quote == null || quote.isBlank()) return identifier;
        return quote + identifier.replace(quote, quote + quote) + quote;
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
}
