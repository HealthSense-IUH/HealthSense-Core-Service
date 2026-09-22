package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** PayOS state for consultation-credit purchases and a cross-domain order-code registry. */
public class V19__credit_payos_payments extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var sql = context.getConnection().createStatement()) {
            sql.execute("""
                ALTER TABLE credit_payment_attempts
                    ADD COLUMN order_code BIGINT,
                    ADD COLUMN payment_link_id VARCHAR(160),
                    ADD COLUMN checkout_url VARCHAR(1000),
                    ADD COLUMN expires_at TIMESTAMPTZ,
                    ADD COLUMN cancelled_at TIMESTAMPTZ,
                    ADD COLUMN last_error VARCHAR(1000)
                """);
            sql.execute("ALTER TABLE credit_payment_attempts DROP CONSTRAINT ck_credit_attempt_paid");
            sql.execute("ALTER TABLE credit_payment_attempts DROP CONSTRAINT credit_payment_attempts_status_check");
            sql.execute("""
                ALTER TABLE credit_payment_attempts ADD CONSTRAINT credit_payment_attempts_status_check
                CHECK (status IN ('CREATING','PENDING','PAID','CANCELLED','EXPIRED','REQUIRES_REVIEW'))
                """);
            sql.execute("CREATE UNIQUE INDEX uq_credit_attempt_order_code ON credit_payment_attempts(order_code) WHERE order_code IS NOT NULL");
            sql.execute("CREATE UNIQUE INDEX uq_credit_attempt_link_id ON credit_payment_attempts(payment_link_id) WHERE payment_link_id IS NOT NULL");
            sql.execute("""
                ALTER TABLE credit_payment_attempts ADD CONSTRAINT ck_credit_attempt_provider_state CHECK (
                    (provider='MOCK' AND order_code IS NULL AND payment_link_id IS NULL AND checkout_url IS NULL AND expires_at IS NULL)
                    OR
                    (provider='PAYOS' AND order_code IS NOT NULL AND expires_at IS NOT NULL)
                )
                """);
            sql.execute("""
                CREATE TABLE payment_order_code_registry (
                    order_code BIGINT PRIMARY KEY,
                    owner_type VARCHAR(40) NOT NULL,
                    owner_id BIGINT NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uq_payment_order_owner UNIQUE(owner_type, owner_id)
                )
                """);
            sql.execute("""
                INSERT INTO payment_order_code_registry(order_code,owner_type,owner_id)
                SELECT order_code,'CONSULTATION_PAYMENT',id FROM consultation_payments WHERE order_code IS NOT NULL
                """);
            sql.execute("CREATE SEQUENCE payment_order_code_seq START WITH 3000000000000000");
            sql.execute("""
                CREATE FUNCTION register_payment_order_code() RETURNS trigger LANGUAGE plpgsql AS $body$
                DECLARE owner VARCHAR(40);
                BEGIN
                    IF NEW.order_code IS NULL OR (TG_OP='UPDATE' AND NEW.order_code IS NOT DISTINCT FROM OLD.order_code) THEN
                        RETURN NEW;
                    END IF;
                    owner := CASE WHEN TG_TABLE_NAME='consultation_payments'
                                  THEN 'CONSULTATION_PAYMENT' ELSE 'CREDIT_PAYMENT_ATTEMPT' END;
                    INSERT INTO payment_order_code_registry(order_code,owner_type,owner_id)
                    VALUES (NEW.order_code,owner,NEW.id);
                    RETURN NEW;
                END;
                $body$
                """);
            sql.execute("CREATE TRIGGER trg_register_consultation_order_code BEFORE INSERT OR UPDATE OF order_code ON consultation_payments FOR EACH ROW EXECUTE FUNCTION register_payment_order_code()");
            sql.execute("CREATE TRIGGER trg_register_credit_order_code BEFORE INSERT OR UPDATE OF order_code ON credit_payment_attempts FOR EACH ROW EXECUTE FUNCTION register_payment_order_code()");
            sql.execute("""
                CREATE OR REPLACE FUNCTION protect_credit_payment_evidence() RETURNS trigger LANGUAGE plpgsql AS $body$
                BEGIN
                    IF ROW(NEW.id,NEW.order_id,NEW.attempt_number,NEW.provider,NEW.order_code,NEW.created_at)
                       IS DISTINCT FROM ROW(OLD.id,OLD.order_id,OLD.attempt_number,OLD.provider,OLD.order_code,OLD.created_at)
                       OR (OLD.payment_link_id IS NOT NULL AND NEW.payment_link_id IS DISTINCT FROM OLD.payment_link_id)
                       OR (OLD.status='PAID' AND ROW(NEW.status,NEW.payment_link_id,NEW.checkout_url,NEW.expires_at,
                              NEW.provider_reference,NEW.verified_amount_vnd,NEW.verified_currency,NEW.paid_at)
                           IS DISTINCT FROM ROW(OLD.status,OLD.payment_link_id,OLD.checkout_url,OLD.expires_at,
                              OLD.provider_reference,OLD.verified_amount_vnd,OLD.verified_currency,OLD.paid_at)) THEN
                        RAISE EXCEPTION 'Credit payment identity/evidence is immutable' USING ERRCODE='23514';
                    END IF;
                    RETURN NEW;
                END;
                $body$
                """);
        }
    }
}
