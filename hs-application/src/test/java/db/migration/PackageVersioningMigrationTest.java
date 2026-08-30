package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageVersioningMigrationTest {

    @Test
    void legacyRowsBecomeVersionOneAndHistoricalReferencesStillResolve() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:package_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        )) {
            createLegacySchema(connection);
            V1__package_versioning_foundation migration = new V1__package_versioning_foundation();

            migration.migrate(context(connection));
            migration.migrate(context(connection));

            try (Statement statement = connection.createStatement()) {
                ResultSet packageRow = statement.executeQuery("""
                        SELECT family_id, version_number, price_amount, currency,
                               duration_days, renewable, status, support_policy
                        FROM care_service_packages
                        WHERE id = 10
                        """);
                assertTrue(packageRow.next());
                assertEquals(10L, packageRow.getLong("family_id"));
                assertEquals(1, packageRow.getInt("version_number"));
                assertEquals(new BigDecimal("990000.00"), packageRow.getBigDecimal("price_amount"));
                assertEquals("VND", packageRow.getString("currency"));
                assertEquals(7, packageRow.getInt("duration_days"));
                assertTrue(packageRow.getBoolean("renewable"));
                assertEquals("ACTIVE", packageRow.getString("status"));
                assertEquals(
                        "ASSIGNED_DOCTOR_SUPPORT_SCHEDULE",
                        packageRow.getString("support_policy")
                );

                ResultSet family = statement.executeQuery("""
                        SELECT code FROM care_service_package_families WHERE id = 10
                        """);
                assertTrue(family.next());
                assertEquals("LEGACY", family.getString("code"));

                assertEquals(
                        "REMOTE_ONE_ON_ONE_CARE",
                        singleString(statement, """
                                SELECT service_code
                                FROM care_service_package_included_services
                                WHERE package_version_id = 10
                                """)
                );
                assertEquals(
                        "EMERGENCY_CARE",
                        singleString(statement, """
                                SELECT service_code
                                FROM care_service_package_excluded_services
                                WHERE package_version_id = 10
                                """)
                );
                assertEquals(
                        1,
                        singleInt(statement, "SELECT package_version FROM consultation_requests WHERE id = 20")
                );
                assertEquals(
                        1,
                        singleInt(statement, "SELECT package_version FROM consultation_sessions WHERE id = 30")
                );
                assertEquals(
                        "LEGACY",
                        singleString(statement, """
                                SELECT p.code
                                FROM consultation_requests r
                                JOIN care_service_packages p ON p.id = r.package_id
                                WHERE r.id = 20
                                """)
                );
            }

            assertThrows(SQLException.class, () -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("""
                            INSERT INTO care_service_packages
                                (id, family_id, code, version_number, name, price_amount, currency,
                                 duration_days, support_policy, renewable, status)
                            VALUES
                                (11, 10, 'LEGACY', 1, 'Duplicate', 1.00, 'VND', 1,
                                 'ASSIGNED_DOCTOR_SUPPORT_SCHEDULE', FALSE, 'INACTIVE')
                            """);
                }
            });
        }
    }

    private void createLegacySchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE care_service_packages (
                        id BIGINT PRIMARY KEY,
                        code VARCHAR(80) NOT NULL,
                        name VARCHAR(160) NOT NULL,
                        description VARCHAR(1000),
                        price_amount NUMERIC(14, 2) NOT NULL,
                        currency VARCHAR(3) NOT NULL,
                        duration_days INTEGER NOT NULL,
                        renewable BOOLEAN NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE,
                        updated_at TIMESTAMP WITH TIME ZONE,
                        created_by VARCHAR(255),
                        updated_by VARCHAR(255),
                        CONSTRAINT uq_care_package_code UNIQUE (code)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE consultation_requests (
                        id BIGINT PRIMARY KEY,
                        package_id BIGINT,
                        package_price_snapshot NUMERIC(14, 2),
                        package_duration_days_snapshot INTEGER
                    )
                    """);
            statement.execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY,
                        package_id BIGINT,
                        package_price_snapshot NUMERIC(14, 2),
                        package_duration_days_snapshot INTEGER
                    )
                    """);
            statement.execute("""
                    INSERT INTO care_service_packages
                        (id, code, name, description, price_amount, currency, duration_days,
                         renewable, status, created_at, updated_at, created_by, updated_by)
                    VALUES
                        (10, 'LEGACY', 'Legacy care', 'Legacy description', 990000.00, 'VND', 7,
                         TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed')
                    """);
            statement.execute("""
                    INSERT INTO consultation_requests
                        (id, package_id, package_price_snapshot, package_duration_days_snapshot)
                    VALUES (20, 10, 990000.00, 7)
                    """);
            statement.execute("""
                    INSERT INTO consultation_sessions
                        (id, package_id, package_price_snapshot, package_duration_days_snapshot)
                    VALUES (30, 10, 990000.00, 7)
                    """);
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

    private String singleString(Statement statement, String sql) throws SQLException {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getString(1);
    }

    private int singleInt(Statement statement, String sql) throws SQLException {
        ResultSet result = statement.executeQuery(sql);
        assertTrue(result.next());
        return result.getInt(1);
    }
}
