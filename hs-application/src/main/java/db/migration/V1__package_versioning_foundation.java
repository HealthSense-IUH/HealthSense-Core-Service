package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V1__package_versioning_foundation extends BaseJavaMigration {

    static final String PACKAGES = "care_service_packages";
    static final String FAMILIES = "care_service_package_families";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createFamilyTable(connection);
        createOrUpgradePackageTable(connection);
        migrateLegacyFamilies(connection);
        createServiceScopeTables(connection);
        migrateLegacyServiceScope(connection);
        protectVersionIntegrity(connection);
        addHistoricalVersionReference(connection, "consultation_requests");
        addHistoricalVersionReference(connection, "consultation_sessions");
    }

    private void createFamilyTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS care_service_package_families (
                    id BIGINT NOT NULL,
                    code VARCHAR(80) NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_care_package_families PRIMARY KEY (id),
                    CONSTRAINT uq_care_package_family_code UNIQUE (code)
                )
                """);
    }

    private void createOrUpgradePackageTable(Connection connection) throws SQLException {
        if (!tableExists(connection, PACKAGES)) {
            execute(connection, """
                    CREATE TABLE care_service_packages (
                        id BIGINT NOT NULL,
                        family_id BIGINT NOT NULL,
                        code VARCHAR(80) NOT NULL,
                        version_number INTEGER NOT NULL,
                        name VARCHAR(160) NOT NULL,
                        short_description VARCHAR(500),
                        description VARCHAR(4000),
                        price_amount NUMERIC(14, 2) NOT NULL,
                        currency VARCHAR(3) NOT NULL,
                        duration_days INTEGER NOT NULL,
                        required_specialty VARCHAR(50),
                        support_policy VARCHAR(80) NOT NULL,
                        renewable BOOLEAN NOT NULL,
                        terms_policy_reference VARCHAR(255),
                        status VARCHAR(30) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE,
                        updated_at TIMESTAMP WITH TIME ZONE,
                        created_by VARCHAR(255),
                        updated_by VARCHAR(255),
                        CONSTRAINT pk_care_service_packages PRIMARY KEY (id)
                    )
                    """);
            return;
        }

        addColumnIfMissing(connection, PACKAGES, "family_id", "BIGINT");
        addColumnIfMissing(connection, PACKAGES, "version_number", "INTEGER");
        addColumnIfMissing(connection, PACKAGES, "short_description", "VARCHAR(500)");
        addColumnIfMissing(connection, PACKAGES, "required_specialty", "VARCHAR(50)");
        addColumnIfMissing(connection, PACKAGES, "support_policy", "VARCHAR(80)");
        addColumnIfMissing(connection, PACKAGES, "terms_policy_reference", "VARCHAR(255)");

        execute(connection, "ALTER TABLE care_service_packages ALTER COLUMN description TYPE VARCHAR(4000)");
        execute(connection, "UPDATE care_service_packages SET family_id = id WHERE family_id IS NULL");
        execute(connection, "UPDATE care_service_packages SET version_number = 1 WHERE version_number IS NULL");
        execute(connection, """
                UPDATE care_service_packages
                SET support_policy = 'ASSIGNED_DOCTOR_SUPPORT_SCHEDULE'
                WHERE support_policy IS NULL
                """);
        execute(connection, "ALTER TABLE care_service_packages ALTER COLUMN family_id SET NOT NULL");
        execute(connection, "ALTER TABLE care_service_packages ALTER COLUMN version_number SET NOT NULL");
        execute(connection, "ALTER TABLE care_service_packages ALTER COLUMN support_policy SET NOT NULL");
    }

    private void migrateLegacyFamilies(Connection connection) throws SQLException {
        execute(connection, """
                INSERT INTO care_service_package_families
                    (id, code, created_at, updated_at, created_by, updated_by)
                SELECT p.family_id, p.code, p.created_at, p.updated_at, p.created_by, p.updated_by
                FROM care_service_packages p
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM care_service_package_families f
                    WHERE f.id = p.family_id
                )
                """);
    }

    private void createServiceScopeTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS care_service_package_included_services (
                    package_version_id BIGINT NOT NULL,
                    service_order INTEGER NOT NULL,
                    service_code VARCHAR(80) NOT NULL,
                    CONSTRAINT pk_care_package_included_services
                        PRIMARY KEY (package_version_id, service_order),
                    CONSTRAINT fk_care_package_included_services_version
                        FOREIGN KEY (package_version_id) REFERENCES care_service_packages (id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS care_service_package_excluded_services (
                    package_version_id BIGINT NOT NULL,
                    service_order INTEGER NOT NULL,
                    service_code VARCHAR(80) NOT NULL,
                    CONSTRAINT pk_care_package_excluded_services
                        PRIMARY KEY (package_version_id, service_order),
                    CONSTRAINT fk_care_package_excluded_services_version
                        FOREIGN KEY (package_version_id) REFERENCES care_service_packages (id)
                )
                """);
    }

    private void migrateLegacyServiceScope(Connection connection) throws SQLException {
        execute(connection, """
                INSERT INTO care_service_package_included_services
                    (package_version_id, service_order, service_code)
                SELECT p.id, 0, 'REMOTE_ONE_ON_ONE_CARE'
                FROM care_service_packages p
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM care_service_package_included_services s
                    WHERE s.package_version_id = p.id
                )
                """);
        execute(connection, """
                INSERT INTO care_service_package_excluded_services
                    (package_version_id, service_order, service_code)
                SELECT p.id, 0, 'EMERGENCY_CARE'
                FROM care_service_packages p
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM care_service_package_excluded_services s
                    WHERE s.package_version_id = p.id
                )
                """);
    }

    private void protectVersionIntegrity(Connection connection) throws SQLException {
        execute(connection, "ALTER TABLE care_service_packages DROP CONSTRAINT IF EXISTS uq_care_package_code");
        if (!constraintExists(connection, PACKAGES, "uq_care_package_family_version")) {
            execute(connection, """
                    ALTER TABLE care_service_packages
                    ADD CONSTRAINT uq_care_package_family_version UNIQUE (family_id, version_number)
                    """);
        }
        if (!constraintExists(connection, PACKAGES, "fk_care_package_family")) {
            execute(connection, """
                    ALTER TABLE care_service_packages
                    ADD CONSTRAINT fk_care_package_family
                    FOREIGN KEY (family_id) REFERENCES care_service_package_families (id)
                    """);
        }
        addCheckConstraintIfMissing(
                connection,
                "ck_care_package_version_positive",
                "version_number > 0"
        );
        addCheckConstraintIfMissing(
                connection,
                "ck_care_package_price_positive",
                "price_amount > 0"
        );
        addCheckConstraintIfMissing(
                connection,
                "ck_care_package_duration_positive",
                "duration_days > 0"
        );
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_care_package_family
                ON care_service_packages (family_id)
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_care_package_status
                ON care_service_packages (status)
                """);

        if (isPostgreSql(connection)) {
            execute(connection, """
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_care_package_one_active_version
                    ON care_service_packages (family_id)
                    WHERE status = 'ACTIVE'
                    """);
        }
    }

    private void addCheckConstraintIfMissing(
            Connection connection,
            String constraint,
            String expression
    ) throws SQLException {
        if (!constraintExists(connection, PACKAGES, constraint)) {
            execute(connection, "ALTER TABLE %s ADD CONSTRAINT %s CHECK (%s)"
                    .formatted(PACKAGES, constraint, expression));
        }
    }

    private void addHistoricalVersionReference(Connection connection, String table) throws SQLException {
        if (!tableExists(connection, table))
            return;
        addColumnIfMissing(connection, table, "package_version", "INTEGER");
        execute(connection, """
                UPDATE %s history
                SET package_version = (
                    SELECT p.version_number
                    FROM care_service_packages p
                    WHERE p.id = history.package_id
                )
                WHERE history.package_id IS NOT NULL
                  AND history.package_version IS NULL
                """.formatted(table));
    }

    private void addColumnIfMissing(
            Connection connection,
            String table,
            String column,
            String definition
    ) throws SQLException {
        if (!columnExists(connection, table, column))
            execute(connection, "ALTER TABLE %s ADD COLUMN %s %s".formatted(table, column, definition));
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (result.next())
                return true;
        }
        try (ResultSet result = metadata.getTables(
                connection.getCatalog(),
                null,
                table.toUpperCase(Locale.ROOT),
                new String[]{"TABLE"}
        )) {
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
                connection.getCatalog(),
                null,
                table.toUpperCase(Locale.ROOT),
                column.toUpperCase(Locale.ROOT)
        )) {
            return result.next();
        }
    }

    private boolean constraintExists(Connection connection, String table, String constraint) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT 1
                FROM information_schema.table_constraints
                WHERE LOWER(table_name) = LOWER(?)
                  AND LOWER(constraint_name) = LOWER(?)
                """)) {
            statement.setString(1, table);
            statement.setString(2, constraint);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
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
