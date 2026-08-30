package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V9__audit_notification_workqueue extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createAuditEvents(connection);
        createNotifications(connection);
        createNeedsActions(connection);
        createProjectionTasks(connection);
    }

    private void createAuditEvents(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS business_audit_events (
                    id BIGINT NOT NULL PRIMARY KEY,
                    domain_type VARCHAR(40) NOT NULL,
                    domain_id BIGINT NOT NULL,
                    event_type VARCHAR(80) NOT NULL,
                    actor_type VARCHAR(20) NOT NULL,
                    actor_user_id BIGINT,
                    actor_role VARCHAR(40),
                    request_id BIGINT,
                    agreement_id BIGINT,
                    payment_id BIGINT,
                    session_id BIGINT,
                    renewal_id BIGINT,
                    refund_id BIGINT,
                    health_record_id BIGINT,
                    member_id BIGINT,
                    doctor_id BIGINT,
                    summary_id BIGINT,
                    previous_state VARCHAR(80),
                    new_state VARCHAR(80),
                    reason VARCHAR(1000),
                    metadata_json VARCHAR(4000),
                    correction_of_event_id BIGINT,
                    idempotency_key VARCHAR(255) UNIQUE,
                    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL
                )
                """);
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_audit_domain_occurred ON business_audit_events (domain_type, domain_id, occurred_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_audit_actor_occurred ON business_audit_events (actor_user_id, occurred_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_audit_event_occurred ON business_audit_events (event_type, occurred_at)");
    }

    private void createNotifications(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS user_notifications (
                    id BIGINT NOT NULL PRIMARY KEY,
                    recipient_id BIGINT NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    message VARCHAR(1000) NOT NULL,
                    reference_type VARCHAR(40),
                    reference_id BIGINT,
                    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                    delivery_status VARCHAR(20) NOT NULL,
                    delivery_error VARCHAR(1000),
                    read_at TIMESTAMP WITH TIME ZONE,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255)
                )
                """);
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_notification_recipient_created ON user_notifications (recipient_id, created_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_notification_recipient_read ON user_notifications (recipient_id, read_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_notification_reference ON user_notifications (reference_type, reference_id)");
    }

    private void createNeedsActions(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS needs_action_items (
                    id BIGINT NOT NULL PRIMARY KEY,
                    type VARCHAR(60) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    priority VARCHAR(20) NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    description VARCHAR(1000) NOT NULL,
                    reference_type VARCHAR(40) NOT NULL,
                    reference_id BIGINT NOT NULL,
                    request_id BIGINT,
                    payment_id BIGINT,
                    session_id BIGINT,
                    renewal_id BIGINT,
                    refund_id BIGINT,
                    member_id BIGINT,
                    doctor_id BIGINT,
                    assigned_role VARCHAR(40) NOT NULL,
                    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                    claimed_by BIGINT,
                    claimed_at TIMESTAMP WITH TIME ZONE,
                    resolved_by BIGINT,
                    resolved_at TIMESTAMP WITH TIME ZONE,
                    resolution VARCHAR(1000),
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255)
                )
                """);
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_needs_action_queue ON needs_action_items (status, assigned_role, priority, created_at)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_needs_action_reference ON needs_action_items (reference_type, reference_id)");
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_needs_action_claimed ON needs_action_items (claimed_by, status)");
    }

    private void createProjectionTasks(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS notification_projection_tasks (
                    id BIGINT NOT NULL PRIMARY KEY,
                    audit_event_id BIGINT NOT NULL UNIQUE,
                    payload_json VARCHAR(8000) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    last_error VARCHAR(1000),
                    next_attempt_at TIMESTAMP WITH TIME ZONE,
                    completed_at TIMESTAMP WITH TIME ZONE,
                    created_at TIMESTAMP WITH TIME ZONE,
                    updated_at TIMESTAMP WITH TIME ZONE,
                    created_by VARCHAR(255),
                    updated_by VARCHAR(255),
                    CONSTRAINT fk_projection_audit FOREIGN KEY (audit_event_id) REFERENCES business_audit_events (id)
                )
                """);
        execute(connection, "CREATE INDEX IF NOT EXISTS idx_notification_projection_retry ON notification_projection_tasks (status, next_attempt_at)");
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
