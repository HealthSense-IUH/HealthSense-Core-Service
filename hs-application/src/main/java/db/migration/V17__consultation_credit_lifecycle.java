package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Captures the immutable credit policy on requests and sessions. */
public class V17__consultation_credit_lifecycle extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var sql = context.getConnection().createStatement()) {
            sql.execute("ALTER TABLE consultation_requests ADD COLUMN credit_policy VARCHAR(30)");
            sql.execute("ALTER TABLE consultation_requests ADD COLUMN credit_cost BIGINT");
            sql.execute("UPDATE consultation_requests SET credit_policy='FREE_EXISTING', credit_cost=0");
            sql.execute("ALTER TABLE consultation_requests ALTER COLUMN credit_policy SET NOT NULL");
            sql.execute("ALTER TABLE consultation_requests ALTER COLUMN credit_cost SET NOT NULL");
            sql.execute("""
                    ALTER TABLE consultation_requests ADD CONSTRAINT ck_consultation_request_credit_policy
                    CHECK ((credit_policy IN ('FREE_EXISTING','FREE_DISABLED') AND credit_cost=0)
                        OR (credit_policy='PER_SESSION_V1' AND credit_cost=1))
                    """);

            sql.execute("ALTER TABLE consultation_sessions ADD COLUMN credit_policy VARCHAR(30)");
            sql.execute("ALTER TABLE consultation_sessions ADD COLUMN credit_cost BIGINT");
            sql.execute("UPDATE consultation_sessions SET credit_policy='FREE_EXISTING', credit_cost=0");
            sql.execute("ALTER TABLE consultation_sessions ALTER COLUMN credit_policy SET NOT NULL");
            sql.execute("ALTER TABLE consultation_sessions ALTER COLUMN credit_cost SET NOT NULL");
            sql.execute("""
                    ALTER TABLE consultation_sessions ADD CONSTRAINT ck_consultation_session_credit_policy
                    CHECK ((credit_policy IN ('FREE_EXISTING','FREE_DISABLED') AND credit_cost=0)
                        OR (credit_policy='PER_SESSION_V1' AND credit_cost=1))
                    """);
        }
    }
}
