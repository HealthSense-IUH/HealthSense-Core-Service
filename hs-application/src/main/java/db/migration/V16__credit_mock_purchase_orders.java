package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Purchases are separate from legacy consultation agreements/payments. No commercial data is seeded. */
public class V16__credit_mock_purchase_orders extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var sql = context.getConnection().createStatement()) {
            sql.execute("""
                CREATE TABLE credit_purchase_orders (
                    id BIGINT PRIMARY KEY,
                    member_id BIGINT NOT NULL REFERENCES user_accounts(id),
                    package_id BIGINT NOT NULL REFERENCES credit_packages(id),
                    package_code VARCHAR(80) NOT NULL, package_name VARCHAR(160) NOT NULL,
                    credit_quantity BIGINT NOT NULL CHECK (credit_quantity > 0),
                    amount_vnd BIGINT NOT NULL CHECK (amount_vnd > 0),
                    currency VARCHAR(3) NOT NULL CHECK (currency = 'VND'),
                    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING_PAYMENT','PAID','CANCELLED','EXPIRED','REQUIRES_REVIEW')),
                    idempotency_key VARCHAR(128) NOT NULL,
                    request_fingerprint VARCHAR(100) NOT NULL,
                    paid_at TIMESTAMPTZ,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ,
                    created_by VARCHAR(255), updated_by VARCHAR(255),
                    CONSTRAINT uq_credit_order_member_key UNIQUE (member_id,idempotency_key),
                    CONSTRAINT ck_credit_order_paid CHECK ((status = 'PAID') = (paid_at IS NOT NULL))
                )
                """);
            sql.execute("CREATE INDEX idx_credit_order_history ON credit_purchase_orders(member_id,created_at DESC,id DESC)");
            sql.execute("""
                CREATE TABLE credit_payment_attempts (
                    id BIGINT PRIMARY KEY,
                    order_id BIGINT NOT NULL REFERENCES credit_purchase_orders(id),
                    attempt_number INTEGER NOT NULL CHECK (attempt_number > 0),
                    provider VARCHAR(20) NOT NULL CHECK (provider IN ('MOCK','PAYOS')),
                    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','PAID')),
                    provider_reference VARCHAR(160),
                    verified_amount_vnd BIGINT, verified_currency VARCHAR(3),
                    paid_at TIMESTAMPTZ,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ,
                    created_by VARCHAR(255), updated_by VARCHAR(255),
                    CONSTRAINT uq_credit_attempt_number UNIQUE (order_id,attempt_number),
                    CONSTRAINT uq_credit_attempt_reference UNIQUE (provider,provider_reference),
                    CONSTRAINT ck_credit_attempt_paid CHECK (
                        (status='PENDING' AND paid_at IS NULL AND provider_reference IS NULL
                         AND verified_amount_vnd IS NULL AND verified_currency IS NULL) OR
                        (status='PAID' AND paid_at IS NOT NULL AND provider_reference IS NOT NULL
                         AND verified_amount_vnd IS NOT NULL AND verified_amount_vnd>0
                         AND verified_currency IS NOT NULL AND verified_currency='VND'))
                )
                """);
            sql.execute("""
                CREATE FUNCTION protect_credit_order_snapshot() RETURNS trigger LANGUAGE plpgsql AS $body$
                BEGIN
                    IF ROW(NEW.id,NEW.member_id,NEW.package_id,NEW.package_code,NEW.package_name,
                           NEW.credit_quantity,NEW.amount_vnd,NEW.currency,NEW.idempotency_key,NEW.request_fingerprint,NEW.created_at)
                       IS DISTINCT FROM
                       ROW(OLD.id,OLD.member_id,OLD.package_id,OLD.package_code,OLD.package_name,
                           OLD.credit_quantity,OLD.amount_vnd,OLD.currency,OLD.idempotency_key,OLD.request_fingerprint,OLD.created_at) THEN
                        RAISE EXCEPTION 'Credit purchase snapshot is immutable' USING ERRCODE='23514';
                    END IF;
                    IF OLD.status='PAID' AND ROW(NEW.status,NEW.paid_at) IS DISTINCT FROM ROW(OLD.status,OLD.paid_at) THEN
                        RAISE EXCEPTION 'A paid credit order cannot be reopened' USING ERRCODE='23514';
                    END IF;
                    RETURN NEW;
                END;
                $body$
                """);
            sql.execute("""
                CREATE TRIGGER trg_credit_order_snapshot BEFORE UPDATE ON credit_purchase_orders
                FOR EACH ROW EXECUTE FUNCTION protect_credit_order_snapshot()
                """);
            sql.execute("""
                CREATE FUNCTION protect_credit_payment_evidence() RETURNS trigger LANGUAGE plpgsql AS $body$
                BEGIN
                    IF ROW(NEW.id,NEW.order_id,NEW.attempt_number,NEW.provider,NEW.created_at)
                       IS DISTINCT FROM ROW(OLD.id,OLD.order_id,OLD.attempt_number,OLD.provider,OLD.created_at)
                       OR (OLD.status='PAID' AND ROW(NEW.status,NEW.provider_reference,NEW.verified_amount_vnd,NEW.verified_currency,NEW.paid_at)
                           IS DISTINCT FROM ROW(OLD.status,OLD.provider_reference,OLD.verified_amount_vnd,OLD.verified_currency,OLD.paid_at)) THEN
                        RAISE EXCEPTION 'Credit payment identity/evidence is immutable' USING ERRCODE='23514';
                    END IF;
                    RETURN NEW;
                END;
                $body$
                """);
            sql.execute("""
                CREATE TRIGGER trg_credit_payment_evidence BEFORE UPDATE ON credit_payment_attempts
                FOR EACH ROW EXECUTE FUNCTION protect_credit_payment_evidence()
                """);
        }
    }
}
