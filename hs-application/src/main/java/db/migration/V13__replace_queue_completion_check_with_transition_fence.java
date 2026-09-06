package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Locale;

/**
 * Replaces V12's row-state check with a transition fence. PostgreSQL checks a
 * NOT VALID constraint whenever an old invalid row is updated, which prevented
 * doctors from finalizing summaries for historical Queue sessions. The trigger
 * rejects newly introduced PERIOD_ENDED values while allowing unrelated updates
 * to rows that were already invalid before the fence existed.
 */
public class V13__replace_queue_completion_check_with_transition_fence extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    ALTER TABLE consultation_sessions
                    DROP CONSTRAINT IF EXISTS ck_queue_dispatch_no_period_ended
                    """);
        }

        boolean postgres = connection.getMetaData().getDatabaseProductName()
                .toLowerCase(Locale.ROOT).contains("postgresql");
        if (!postgres) return;

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE OR REPLACE FUNCTION prevent_queue_dispatch_period_ended_transition()
                    RETURNS trigger
                    LANGUAGE plpgsql
                    AS $function$
                    BEGIN
                        IF NEW.flow_type = 'QUEUE_DISPATCH_V1'
                           AND NEW.completion_reason = 'PERIOD_ENDED'
                           AND (
                               TG_OP = 'INSERT'
                               OR OLD.flow_type IS DISTINCT FROM 'QUEUE_DISPATCH_V1'
                               OR OLD.completion_reason IS DISTINCT FROM 'PERIOD_ENDED'
                           ) THEN
                            RAISE EXCEPTION
                                'Queue Dispatch sessions cannot be completed by the legacy period-ended lifecycle'
                                USING ERRCODE = '23514',
                                      CONSTRAINT = 'trg_queue_dispatch_no_period_ended_transition';
                        END IF;
                        RETURN NEW;
                    END;
                    $function$
                    """);
            statement.execute("""
                    DROP TRIGGER IF EXISTS trg_queue_dispatch_no_period_ended_transition
                    ON consultation_sessions
                    """);
            statement.execute("""
                    CREATE TRIGGER trg_queue_dispatch_no_period_ended_transition
                    BEFORE INSERT OR UPDATE OF flow_type, completion_reason
                    ON consultation_sessions
                    FOR EACH ROW
                    EXECUTE FUNCTION prevent_queue_dispatch_period_ended_transition()
                    """);
        }
    }
}
