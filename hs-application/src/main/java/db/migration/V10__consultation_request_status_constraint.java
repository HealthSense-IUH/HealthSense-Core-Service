package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V10__consultation_request_status_constraint extends BaseJavaMigration {

    static final String REQUESTS = "consultation_requests";
    static final String STATUS_CONSTRAINT = "consultation_requests_status_check";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, REQUESTS) || !columnExists(connection, REQUESTS, "status"))
            return;

        execute(connection, "ALTER TABLE consultation_requests DROP CONSTRAINT IF EXISTS " + STATUS_CONSTRAINT);
        execute(connection, """
                ALTER TABLE consultation_requests
                ADD CONSTRAINT consultation_requests_status_check
                CHECK (status IN (
                    'PENDING_REVIEW',
                    'NEED_MORE_INFO',
                    'WAITING_ACCEPTANCE',
                    'WAITING_PAYMENT',
                    'FULFILLED',
                    'REJECTED',
                    'CANCELLED',
                    'EXPIRED'
                ))
                """);
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (result.next())
                return true;
        }
        try (ResultSet result = metadata.getTables(
                connection.getCatalog(), null, table.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            if (result.next())
                return true;
        }
        try (ResultSet result = metadata.getColumns(
                connection.getCatalog(), null, table.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT))) {
            return result.next();
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
