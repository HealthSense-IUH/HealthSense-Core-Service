package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Locale;

public class V7_1__renewal_review_integrity extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgres"))
            return;
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP INDEX IF EXISTS uq_unresolved_renewal_per_session");
            statement.execute("""
                    CREATE UNIQUE INDEX uq_unresolved_renewal_per_session
                    ON consultation_renewals (session_id)
                    WHERE status IN (
                        'REQUESTED','UNDER_REVIEW','APPROVED','PENDING_ACCEPTANCE',
                        'WAITING_PAYMENT','PAID','REQUIRES_REVIEW'
                    )
                    """);
        }
    }
}
