package fit.iuh.se.hsoperations.entity;

import fit.iuh.se.hsoperations.entity.enums.NotificationProjectionStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "notification_projection_tasks", indexes =
        @Index(name = "idx_notification_projection_retry", columnList = "status, next_attempt_at"),
        uniqueConstraints = @UniqueConstraint(name = "uq_notification_projection_audit", columnNames = "audit_event_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationProjectionTask extends BaseEntity {
    @Id @SnowflakeGenerated @Column(nullable = false, updatable = false) Long id;
    @Column(name = "audit_event_id", nullable = false, updatable = false) Long auditEventId;
    @Column(name = "payload_json", nullable = false, updatable = false, length = 8000) String payloadJson;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) NotificationProjectionStatus status;
    @Column(name = "attempts", nullable = false) Integer attempts;
    @Column(name = "last_error", length = 1000) String lastError;
    @Column(name = "next_attempt_at") Instant nextAttemptAt;
    @Column(name = "completed_at") Instant completedAt;
}
