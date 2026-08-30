package fit.iuh.se.hsoperations.entity;

import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "business_audit_events", indexes = {
        @Index(name = "idx_audit_domain_occurred", columnList = "domain_type, domain_id, occurred_at"),
        @Index(name = "idx_audit_actor_occurred", columnList = "actor_user_id, occurred_at"),
        @Index(name = "idx_audit_event_occurred", columnList = "event_type, occurred_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BusinessAuditEvent {
    @Id @SnowflakeGenerated @Column(nullable = false, updatable = false) Long id;
    @Enumerated(EnumType.STRING) @Column(name = "domain_type", nullable = false, updatable = false, length = 40) BusinessDomainType domainType;
    @Column(name = "domain_id", nullable = false, updatable = false) Long domainId;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, updatable = false, length = 80) BusinessEventType eventType;
    @Enumerated(EnumType.STRING) @Column(name = "actor_type", nullable = false, updatable = false, length = 20) BusinessActorType actorType;
    @Column(name = "actor_user_id", updatable = false) Long actorUserId;
    @Column(name = "actor_role", updatable = false, length = 40) String actorRole;
    @Column(name = "request_id", updatable = false) Long requestId;
    @Column(name = "agreement_id", updatable = false) Long agreementId;
    @Column(name = "payment_id", updatable = false) Long paymentId;
    @Column(name = "session_id", updatable = false) Long sessionId;
    @Column(name = "renewal_id", updatable = false) Long renewalId;
    @Column(name = "refund_id", updatable = false) Long refundId;
    @Column(name = "health_record_id", updatable = false) Long healthRecordId;
    @Column(name = "member_id", updatable = false) Long memberId;
    @Column(name = "doctor_id", updatable = false) Long doctorId;
    @Column(name = "summary_id", updatable = false) Long summaryId;
    @Column(name = "previous_state", updatable = false, length = 80) String previousState;
    @Column(name = "new_state", updatable = false, length = 80) String newState;
    @Column(name = "reason", updatable = false, length = 1000) String reason;
    @Column(name = "metadata_json", updatable = false, length = 4000) String metadataJson;
    @Column(name = "correction_of_event_id", updatable = false) Long correctionOfEventId;
    @Column(name = "idempotency_key", updatable = false, unique = true, length = 255) String idempotencyKey;
    @Column(name = "occurred_at", nullable = false, updatable = false) Instant occurredAt;
    @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;

    @PrePersist
    void initializeTimestamps() {
        if (occurredAt == null) occurredAt = Instant.now();
        if (createdAt == null) createdAt = Instant.now();
    }
}
