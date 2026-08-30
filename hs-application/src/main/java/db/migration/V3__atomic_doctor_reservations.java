package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.Locale;

public class V3__atomic_doctor_reservations extends BaseJavaMigration {

    static final String REQUESTS = "consultation_requests";
    static final String RESERVATIONS = "doctor_reservations";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, REQUESTS))
            return;

        execute(connection, """
                CREATE TABLE IF NOT EXISTS doctor_reservations (
                    id BIGINT NOT NULL,
                    request_id BIGINT NOT NULL,
                    doctor_id BIGINT NOT NULL,
                    package_id BIGINT,
                    package_version INTEGER,
                    reserved_by BIGINT,
                    reserved_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    release_reason VARCHAR(50),
                    released_at TIMESTAMP WITH TIME ZONE,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_doctor_reservations PRIMARY KEY (id),
                    CONSTRAINT fk_doctor_reservation_request
                        FOREIGN KEY (request_id) REFERENCES consultation_requests (id)
                )
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_reservation_request_status
                ON doctor_reservations (request_id, status)
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_reservation_doctor_capacity
                ON doctor_reservations (doctor_id, status, expires_at)
                """);

        migrateCurrentAssignments(connection);

        if (isPostgreSql(connection)) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_reservation_one_active_per_request
                    ON doctor_reservations (request_id)
                    WHERE status = 'ACTIVE'
                    """);
        }
    }

    private void migrateCurrentAssignments(Connection connection) throws SQLException {
        if (!columnExists(connection, REQUESTS, "assigned_doctor_id")
                || !columnExists(connection, REQUESTS, "doctor_reserved_at")
                || !columnExists(connection, REQUESTS, "payment_deadline")
                || !columnExists(connection, REQUESTS, "status"))
            return;

        String packageId = columnExists(connection, REQUESTS, "package_id") ? "r.package_id" : "NULL";
        String packageVersion = columnExists(connection, REQUESTS, "package_version") ? "r.package_version" : "NULL";
        String reservedBy = columnExists(connection, REQUESTS, "reviewed_by_admin_id")
                ? "r.reviewed_by_admin_id" : "NULL";
        execute(connection, """
                INSERT INTO doctor_reservations (
                    id, request_id, doctor_id, package_id, package_version, reserved_by,
                    reserved_at, expires_at, status, version, created_at, updated_at
                )
                SELECT r.id, r.id, r.assigned_doctor_id, %s, %s, %s,
                       r.doctor_reserved_at, r.payment_deadline, 'ACTIVE', 0,
                       r.doctor_reserved_at, r.doctor_reserved_at
                FROM consultation_requests r
                WHERE r.assigned_doctor_id IS NOT NULL
                  AND r.doctor_reserved_at IS NOT NULL
                  AND r.payment_deadline IS NOT NULL
                  AND r.status IN ('WAITING_ACCEPTANCE', 'WAITING_PAYMENT')
                  AND NOT EXISTS (
                      SELECT 1 FROM doctor_reservations reservation
                      WHERE reservation.request_id = r.id AND reservation.status = 'ACTIVE'
                  )
                """.formatted(packageId, packageVersion, reservedBy));
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

    private boolean isPostgreSql(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgresql");
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
