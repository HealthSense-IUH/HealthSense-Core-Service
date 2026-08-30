package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationRenewalStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "consultation_renewals", indexes = {
        @Index(name = "idx_renewal_session_status", columnList = "session_id, status"),
        @Index(name = "idx_renewal_doctor_window", columnList = "doctor_id, previous_ends_at, proposed_new_ends_at"),
        @Index(name = "idx_renewal_member_created", columnList = "member_id, created_at")
})
public class ConsultationRenewal extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "session_id", nullable = false, updatable = false)
    Long sessionId;

    @Column(name = "member_id", nullable = false, updatable = false)
    Long memberId;

    @Column(name = "doctor_id", nullable = false, updatable = false)
    Long doctorId;

    @Column(name = "package_family_id", nullable = false, updatable = false)
    Long packageFamilyId;

    @Column(name = "package_id")
    Long packageId;

    @Column(name = "package_version")
    Integer packageVersion;

    @Column(name = "duration_days")
    Integer durationDays;

    @Column(name = "price_amount", precision = 14, scale = 2)
    BigDecimal priceAmount;

    @Column(name = "currency", length = 3)
    String currency;

    @Column(name = "support_schedule_snapshot_json", columnDefinition = "TEXT")
    String supportScheduleSnapshotJson;

    @Column(name = "support_timezone_snapshot", length = 80)
    String supportTimezoneSnapshot;

    @Column(name = "previous_ends_at")
    Instant previousEndsAt;

    @Column(name = "proposed_new_ends_at")
    Instant proposedNewEndsAt;

    @Column(name = "agreement_id")
    Long agreementId;

    @Column(name = "successful_payment_id")
    Long successfulPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    ConsultationRenewalStatus status;

    @Column(name = "requested_at", nullable = false, updatable = false)
    Instant requestedAt;

    @Column(name = "reviewed_by")
    Long reviewedBy;

    @Column(name = "review_started_at")
    Instant reviewStartedAt;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    String rejectionReason;

    @Column(name = "payment_deadline")
    Instant paymentDeadline;

    @Column(name = "applied_at")
    Instant appliedAt;

    @Version
    @Column(nullable = false)
    long version;
}
