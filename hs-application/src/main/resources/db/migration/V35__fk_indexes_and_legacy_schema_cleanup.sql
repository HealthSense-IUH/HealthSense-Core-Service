-- 1. Add missing indexes on Foreign Key columns for join/delete performance
CREATE INDEX IF NOT EXISTS idx_final_summary_addenda_session_id
    ON consultation_final_summary_addenda (session_id);

CREATE INDEX IF NOT EXISTS idx_credit_ledger_entries_related_entry_id
    ON credit_ledger_entries (related_entry_id);

CREATE INDEX IF NOT EXISTS idx_credit_purchase_orders_package_id
    ON credit_purchase_orders (package_id);

CREATE INDEX IF NOT EXISTS idx_doctor_care_profiles_busy_session_id
    ON doctor_care_profiles (busy_session_id);

-- 2. Remove unused legacy schema artifacts (roles now live on user_accounts.role; status replaces active)
DROP TABLE IF EXISTS user_roles;
ALTER TABLE IF EXISTS user_accounts DROP COLUMN IF EXISTS active;
ALTER TABLE IF EXISTS user_profiles DROP COLUMN IF EXISTS active;
