package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V5__episode_health_record_authorization extends BaseJavaMigration {

    private static final String SESSIONS = "consultation_sessions";
    private static final String AUTHORIZATIONS = "consultation_episode_health_records";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, SESSIONS))
            return;

        addActivatedAt(connection);
        createAuthorizationTable(connection);
        backfillExplicitEpisodeRecords(connection);
    }

    private void addActivatedAt(Connection connection) throws SQLException {
        if (!columnExists(connection, SESSIONS, "activated_at"))
            execute(connection, "ALTER TABLE consultation_sessions ADD COLUMN activated_at TIMESTAMP WITH TIME ZONE");

        execute(connection, """
                UPDATE consultation_sessions
                SET activated_at = COALESCE(started_at, created_at, CURRENT_TIMESTAMP)
                WHERE activated_at IS NULL
                  AND status IN ('ACTIVE', 'COMPLETED')
                """);
    }

    private void createAuthorizationTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_episode_health_records (
                    id BIGINT NOT NULL,
                    session_id BIGINT NOT NULL,
                    health_record_id BIGINT NOT NULL,
                    member_id BIGINT NOT NULL,
                    authorization_source VARCHAR(40) NOT NULL,
                    authorized_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    authorized_by BIGINT,
                    authorized_by_type VARCHAR(30) NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT pk_consultation_episode_health_records PRIMARY KEY (id),
                    CONSTRAINT uq_episode_health_record UNIQUE (session_id, health_record_id),
                    CONSTRAINT fk_episode_health_record_session FOREIGN KEY (session_id)
                        REFERENCES consultation_sessions (id)
                )
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_episode_hr_session
                ON consultation_episode_health_records (session_id, authorized_at)
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_episode_hr_record
                ON consultation_episode_health_records (health_record_id)
                """);
        execute(connection, """
                CREATE INDEX IF NOT EXISTS idx_episode_hr_member
                ON consultation_episode_health_records (member_id, session_id)
                """);
    }

    private void backfillExplicitEpisodeRecords(Connection connection) throws SQLException {
        if (columnExists(connection, SESSIONS, "health_record_id")) {
            insertExplicitRecords(connection, """
                    SELECT session.id AS session_id, session.health_record_id, session.member_id,
                           session.activated_at AS authorized_at
                    FROM consultation_sessions session
                    WHERE session.activated_at IS NOT NULL
                      AND session.health_record_id IS NOT NULL
                    """);
        }

        if (columnExists(connection, SESSIONS, "request_id")
                && tableExists(connection, "consultation_request_health_records")) {
            insertExplicitRecords(connection, """
                    SELECT session.id AS session_id, selected.health_record_id, session.member_id,
                           session.activated_at AS authorized_at
                    FROM consultation_sessions session
                    JOIN consultation_request_health_records selected
                      ON selected.request_id = session.request_id
                    WHERE session.activated_at IS NOT NULL
                    """);
        }
    }

    private void insertExplicitRecords(Connection connection, String sourceQuery) throws SQLException {
        execute(connection, """
                INSERT INTO consultation_episode_health_records (
                    id, session_id, health_record_id, member_id, authorization_source,
                    authorized_at, authorized_by, authorized_by_type, created_at, updated_at
                )
                SELECT base.max_id + ROW_NUMBER() OVER (ORDER BY candidate.session_id, candidate.health_record_id),
                       candidate.session_id, candidate.health_record_id, candidate.member_id,
                       'INITIAL_SHARED', candidate.authorized_at, candidate.member_id, 'SYSTEM',
                       candidate.authorized_at, candidate.authorized_at
                FROM (%s) candidate
                CROSS JOIN (
                    SELECT COALESCE(MAX(id), 0) AS max_id
                    FROM consultation_episode_health_records
                ) base
                WHERE NOT EXISTS (
                    SELECT 1 FROM consultation_episode_health_records existing
                    WHERE existing.session_id = candidate.session_id
                      AND existing.health_record_id = candidate.health_record_id
                )
                """.formatted(sourceQuery));
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (result.next()) return true;
        }
        try (ResultSet result = metadata.getTables(connection.getCatalog(), null,
                table.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            if (result.next()) return true;
        }
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null,
                table.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT))) {
            return result.next();
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
