package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Statement;

/** Additive PostgreSQL migration; existing consultation/payment data is untouched. */
public class V15__consultation_credit_foundation extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (Statement sql = context.getConnection().createStatement()) {
            sql.execute("""
                CREATE TABLE credit_packages (
                    id BIGINT PRIMARY KEY,
                    code VARCHAR(80) NOT NULL UNIQUE,
                    name VARCHAR(160) NOT NULL,
                    description VARCHAR(1000),
                    credit_quantity BIGINT NOT NULL CHECK (credit_quantity > 0),
                    price_vnd BIGINT NOT NULL CHECK (price_vnd > 0),
                    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
                    created_by VARCHAR(255), updated_by VARCHAR(255)
                )
                """);
            sql.execute("CREATE INDEX idx_credit_package_status ON credit_packages(status)");
            sql.execute("""
                CREATE TABLE credit_wallets (
                    id BIGINT PRIMARY KEY,
                    member_id BIGINT NOT NULL UNIQUE REFERENCES user_accounts(id),
                    balance BIGINT NOT NULL DEFAULT 0,
                    reserved BIGINT NOT NULL DEFAULT 0,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
                    created_by VARCHAR(255), updated_by VARCHAR(255),
                    CONSTRAINT ck_credit_wallet_balance CHECK (balance >= 0 AND reserved >= 0 AND reserved <= balance)
                )
                """);
            sql.execute("""
                CREATE TABLE credit_reservations (
                    id BIGINT PRIMARY KEY,
                    wallet_id BIGINT NOT NULL REFERENCES credit_wallets(id),
                    request_id BIGINT NOT NULL UNIQUE,
                    quantity BIGINT NOT NULL CHECK (quantity > 0),
                    status VARCHAR(20) NOT NULL,
                    session_id BIGINT UNIQUE,
                    held_at TIMESTAMPTZ NOT NULL,
                    captured_at TIMESTAMPTZ, released_at TIMESTAMPTZ,
                    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
                    created_by VARCHAR(255), updated_by VARCHAR(255),
                    CONSTRAINT ck_credit_reservation_state CHECK (
                        (status = 'HELD' AND session_id IS NULL AND captured_at IS NULL AND released_at IS NULL) OR
                        (status = 'CAPTURED' AND session_id IS NOT NULL AND captured_at IS NOT NULL AND released_at IS NULL) OR
                        (status = 'RELEASED' AND session_id IS NULL AND captured_at IS NULL AND released_at IS NOT NULL)
                    )
                )
                """);
            sql.execute("CREATE INDEX idx_credit_reservation_status ON credit_reservations(status, held_at)");
            sql.execute("CREATE INDEX idx_credit_reservation_wallet ON credit_reservations(wallet_id)");
            sql.execute("""
                CREATE TABLE credit_ledger_entries (
                    id BIGINT PRIMARY KEY,
                    wallet_id BIGINT NOT NULL REFERENCES credit_wallets(id),
                    operation VARCHAR(30) NOT NULL,
                    quantity BIGINT NOT NULL CHECK (quantity > 0),
                    delta_balance BIGINT NOT NULL, delta_reserved BIGINT NOT NULL,
                    balance_after BIGINT NOT NULL, reserved_after BIGINT NOT NULL,
                    source_type VARCHAR(30) NOT NULL,
                    source_id BIGINT NOT NULL,
                    related_entry_id BIGINT REFERENCES credit_ledger_entries(id),
                    idempotency_key VARCHAR(180) NOT NULL UNIQUE,
                    actor_id BIGINT,
                    reason VARCHAR(500),
                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ,
                    created_by VARCHAR(255), updated_by VARCHAR(255),
                    CONSTRAINT uq_credit_ledger_source UNIQUE (operation, source_type, source_id),
                    CONSTRAINT ck_credit_ledger_balance CHECK (
                        balance_after >= 0 AND reserved_after >= 0 AND reserved_after <= balance_after),
                    CONSTRAINT ck_credit_ledger_source CHECK (
                        (operation = 'PURCHASE' AND source_type = 'PURCHASE_ORDER') OR
                        (operation IN ('RESERVE','RELEASE') AND source_type = 'CONSULTATION_REQUEST') OR
                        (operation IN ('CAPTURE','SESSION_REFUND') AND source_type = 'CONSULTATION_SESSION') OR
                        operation = 'ADJUSTMENT'),
                    CONSTRAINT ck_credit_ledger_delta CHECK (
                        (operation IN ('PURCHASE','SESSION_REFUND') AND delta_balance = quantity AND delta_reserved = 0) OR
                        (operation = 'RESERVE' AND delta_balance = 0 AND delta_reserved = quantity) OR
                        (operation = 'CAPTURE' AND delta_balance = -quantity AND delta_reserved = -quantity) OR
                        (operation = 'RELEASE' AND delta_balance = 0 AND delta_reserved = -quantity) OR
                        (operation = 'ADJUSTMENT' AND delta_reserved = 0 AND delta_balance IN (quantity, -quantity)))
                )
                """);
            sql.execute("CREATE INDEX idx_credit_ledger_history ON credit_ledger_entries(wallet_id, created_at DESC, id DESC)");
            sql.execute("""
                CREATE FUNCTION reject_credit_ledger_mutation() RETURNS trigger LANGUAGE plpgsql AS $body$
                BEGIN
                    RAISE EXCEPTION 'Credit ledger is append-only' USING ERRCODE = '23514';
                END;
                $body$
                """);
            sql.execute("""
                CREATE TRIGGER trg_credit_ledger_immutable BEFORE UPDATE OR DELETE ON credit_ledger_entries
                FOR EACH ROW EXECUTE FUNCTION reject_credit_ledger_mutation()
                """);
        }
    }
}
