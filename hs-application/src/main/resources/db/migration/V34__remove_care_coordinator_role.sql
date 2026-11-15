-- Fold the retired operational role into ADMIN before the Java enum is read.
UPDATE user_accounts SET role = 'ADMIN' WHERE role = 'CARE_COORDINATOR';
UPDATE business_audit_events SET actor_role = 'ADMIN' WHERE actor_role = 'CARE_COORDINATOR';
UPDATE needs_action_items SET assigned_role = 'ADMIN' WHERE assigned_role = 'CARE_COORDINATOR';
UPDATE consultation_sessions SET termination_requested_by_role = 'ADMIN'
WHERE termination_requested_by_role = 'CARE_COORDINATOR';
UPDATE consultation_sessions SET termination_decided_by_role = 'ADMIN'
WHERE termination_decided_by_role = 'CARE_COORDINATOR';
UPDATE notification_projection_tasks
SET payload_json = REPLACE(payload_json, 'CARE_COORDINATOR', 'ADMIN')
WHERE payload_json LIKE '%CARE_COORDINATOR%';
UPDATE doctor_reservations SET release_reason = 'ADMIN_REJECTED'
WHERE release_reason = 'COORDINATOR_REJECTED';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'consultation_more_info_cycles'
          AND column_name = 'coordinator_message'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'consultation_more_info_cycles'
          AND column_name = 'reviewer_message'
    ) THEN
        ALTER TABLE consultation_more_info_cycles
            RENAME COLUMN coordinator_message TO reviewer_message;
    END IF;
END $$;

-- Keep existing constraint names while replacing retired enum literals.
DO $$
DECLARE
    constraint_row RECORD;
    replacement_definition TEXT;
BEGIN
    FOR constraint_row IN
        SELECT ns.nspname AS schema_name,
               relation.relname AS table_name,
               constraint_record.conname AS constraint_name,
               pg_get_constraintdef(constraint_record.oid) AS definition
        FROM pg_constraint constraint_record
        JOIN pg_class relation ON relation.oid = constraint_record.conrelid
        JOIN pg_namespace ns ON ns.oid = relation.relnamespace
        WHERE constraint_record.contype = 'c'
          AND ns.nspname = current_schema()
          AND (pg_get_constraintdef(constraint_record.oid) LIKE '%CARE_COORDINATOR%'
               OR pg_get_constraintdef(constraint_record.oid) LIKE '%COORDINATOR_REJECTED%')
    LOOP
        replacement_definition := REPLACE(
            REPLACE(constraint_row.definition, 'CARE_COORDINATOR', 'ADMIN'),
            'COORDINATOR_REJECTED', 'ADMIN_REJECTED'
        );
        EXECUTE format('ALTER TABLE %I.%I DROP CONSTRAINT %I',
            constraint_row.schema_name, constraint_row.table_name, constraint_row.constraint_name);
        EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I %s',
            constraint_row.schema_name, constraint_row.table_name,
            constraint_row.constraint_name, replacement_definition);
    END LOOP;
END $$;
