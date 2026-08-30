package fit.iuh.se.hsoperations.entity;

import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "needs_action_items", indexes = {
        @Index(name = "idx_needs_action_queue", columnList = "status, assigned_role, priority, created_at"),
        @Index(name = "idx_needs_action_reference", columnList = "reference_type, reference_id"),
        @Index(name = "idx_needs_action_claimed", columnList = "claimed_by, status")
}, uniqueConstraints = @UniqueConstraint(name = "uq_needs_action_idempotency", columnNames = "idempotency_key"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NeedsActionItem extends BaseEntity {
    @Id @SnowflakeGenerated @Column(nullable = false, updatable = false) Long id;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false, updatable = false, length = 60) NeedsActionType type;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) NeedsActionStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "priority", nullable = false, length = 20) NeedsActionPriority priority;
    @Column(name = "title", nullable = false, length = 200) String title;
    @Column(name = "description", nullable = false, length = 1000) String description;
    @Enumerated(EnumType.STRING) @Column(name = "reference_type", nullable = false, updatable = false, length = 40) BusinessDomainType referenceType;
    @Column(name = "reference_id", nullable = false, updatable = false) Long referenceId;
    @Column(name = "request_id", updatable = false) Long requestId;
    @Column(name = "payment_id", updatable = false) Long paymentId;
    @Column(name = "session_id", updatable = false) Long sessionId;
    @Column(name = "renewal_id", updatable = false) Long renewalId;
    @Column(name = "refund_id", updatable = false) Long refundId;
    @Column(name = "member_id", updatable = false) Long memberId;
    @Column(name = "doctor_id", updatable = false) Long doctorId;
    @Column(name = "assigned_role", nullable = false, length = 40) String assignedRole;
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 255) String idempotencyKey;
    @Column(name = "claimed_by") Long claimedBy;
    @Column(name = "claimed_at") Instant claimedAt;
    @Column(name = "resolved_by") Long resolvedBy;
    @Column(name = "resolved_at") Instant resolvedAt;
    @Column(name = "resolution", length = 1000) String resolution;
}
