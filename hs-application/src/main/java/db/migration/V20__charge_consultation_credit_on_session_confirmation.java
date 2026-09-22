package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** New queue requests check credit at admission and charge only when the session is committed. */
public class V20__charge_consultation_credit_on_session_confirmation extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var sql = context.getConnection().createStatement()) {
            sql.execute("ALTER TABLE consultation_requests DROP CONSTRAINT ck_consultation_request_credit_policy");
            sql.execute("""
                    ALTER TABLE consultation_requests ADD CONSTRAINT ck_consultation_request_credit_policy
                    CHECK ((credit_policy IN ('FREE_EXISTING','FREE_DISABLED') AND credit_cost=0)
                        OR (credit_policy IN ('PER_SESSION_V1','PER_SESSION_CONFIRM_V2') AND credit_cost=1))
                    """);
            sql.execute("ALTER TABLE consultation_sessions DROP CONSTRAINT ck_consultation_session_credit_policy");
            sql.execute("""
                    ALTER TABLE consultation_sessions ADD CONSTRAINT ck_consultation_session_credit_policy
                    CHECK ((credit_policy IN ('FREE_EXISTING','FREE_DISABLED') AND credit_cost=0)
                        OR (credit_policy IN ('PER_SESSION_V1','PER_SESSION_CONFIRM_V2') AND credit_cost=1))
                    """);

            sql.execute("ALTER TABLE credit_ledger_entries DROP CONSTRAINT ck_credit_ledger_source");
            sql.execute("""
                    ALTER TABLE credit_ledger_entries ADD CONSTRAINT ck_credit_ledger_source CHECK (
                        (operation = 'PURCHASE' AND source_type = 'PURCHASE_ORDER') OR
                        (operation IN ('RESERVE','RELEASE') AND source_type = 'CONSULTATION_REQUEST') OR
                        (operation IN ('CAPTURE','SESSION_CHARGE','SESSION_REFUND') AND source_type = 'CONSULTATION_SESSION') OR
                        (operation = 'ADJUSTMENT' AND source_type = 'ADMIN_ADJUSTMENT'))
                    """);
            sql.execute("ALTER TABLE credit_ledger_entries DROP CONSTRAINT ck_credit_ledger_delta");
            sql.execute("""
                    ALTER TABLE credit_ledger_entries ADD CONSTRAINT ck_credit_ledger_delta CHECK (
                        (operation IN ('PURCHASE','SESSION_REFUND') AND delta_balance = quantity AND delta_reserved = 0) OR
                        (operation = 'RESERVE' AND delta_balance = 0 AND delta_reserved = quantity) OR
                        (operation = 'CAPTURE' AND delta_balance = -quantity AND delta_reserved = -quantity) OR
                        (operation = 'SESSION_CHARGE' AND delta_balance = -quantity AND delta_reserved = 0) OR
                        (operation = 'RELEASE' AND delta_balance = 0 AND delta_reserved = -quantity) OR
                        (operation = 'ADJUSTMENT' AND delta_reserved = 0 AND delta_balance IN (quantity, -quantity)))
                    """);
        }
    }
}
