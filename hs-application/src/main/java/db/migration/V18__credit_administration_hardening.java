package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Tightens administrative credit provenance without rewriting existing ledger history. */
public class V18__credit_administration_hardening extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var sql = context.getConnection().createStatement()) {
            sql.execute("ALTER TABLE credit_ledger_entries DROP CONSTRAINT ck_credit_ledger_source");
            sql.execute("""
                ALTER TABLE credit_ledger_entries ADD CONSTRAINT ck_credit_ledger_source CHECK (
                    (operation = 'PURCHASE' AND source_type = 'PURCHASE_ORDER') OR
                    (operation IN ('RESERVE','RELEASE') AND source_type = 'CONSULTATION_REQUEST') OR
                    (operation IN ('CAPTURE','SESSION_REFUND') AND source_type = 'CONSULTATION_SESSION') OR
                    (operation = 'ADJUSTMENT' AND source_type = 'ADMIN_ADJUSTMENT'))
                """);
            sql.execute("""
                ALTER TABLE credit_ledger_entries ADD CONSTRAINT ck_credit_ledger_admin_provenance CHECK (
                    operation NOT IN ('ADJUSTMENT','SESSION_REFUND') OR
                    (actor_id IS NOT NULL AND reason IS NOT NULL AND length(trim(reason)) > 0))
                """);
            sql.execute("CREATE INDEX idx_credit_orders_admin_search ON credit_purchase_orders(created_at DESC, status, member_id)");
            sql.execute("CREATE INDEX idx_credit_attempt_order_provider ON credit_payment_attempts(order_id, provider)");
        }
    }
}
