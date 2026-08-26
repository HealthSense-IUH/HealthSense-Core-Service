package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "consultation_requests",
        indexes = {
                @Index(name = "idx_request_member_status", columnList = "member_id, status"),
                @Index(name = "idx_request_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_request_assigned_doctor", columnList = "assigned_doctor_id"),
                @Index(name = "idx_request_health_record", columnList = "health_record_id"),
                @Index(name = "idx_request_package", columnList = "package_id"),
                @Index(name = "idx_request_payment_deadline", columnList = "status, payment_deadline")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_request_consultation_session", columnNames = "consultation_session_id")
        }
)
public class ConsultationRequest extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "member_id", nullable = false)
    Long memberId;

    @Column(name = "health_record_id")
    Long healthRecordId;

    @Column(name = "package_id")
    Long packageId;

    @Column(name = "package_version")
    Integer packageVersion;

    @Column(name = "package_price_snapshot", precision = 14, scale = 2)
    BigDecimal packagePriceSnapshot;

    @Column(name = "package_duration_days_snapshot")
    Integer packageDurationDaysSnapshot;

    @Column(name = "reason", nullable = false, length = 1000)
    String reason;

    @Column(name = "reason_for_care", length = 1000)
    String reasonForCare;

    @Column(name = "current_concern", length = 2000)
    String currentConcern;

    @Column(name = "care_goal", length = 1000)
    String careGoal;

    @Column(name = "member_note", length = 1000)
    String memberNote;

    @Column(name = "relevant_self_reported_context", length = 4000)
    String relevantSelfReportedContext;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "consultation_request_health_records",
            joinColumns = @JoinColumn(name = "request_id")
    )
    @Column(name = "health_record_id", nullable = false)
    @OrderColumn(name = "selection_order")
    @Builder.Default
    List<Long> selectedHealthRecordIds = new ArrayList<>();

    @Column(name = "intake_frozen_at")
    Instant intakeFrozenAt;

    @Column(name = "preferred_doctor_id")
    Long preferredDoctorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    ConsultationRequestStatus status;

    @Column(name = "assigned_doctor_id")
    Long assignedDoctorId;

    @Column(name = "doctor_reserved_at")
    Instant doctorReservedAt;

    @Column(name = "payment_deadline")
    Instant paymentDeadline;

    @Column(name = "consultation_session_id", unique = true)
    Long consultationSessionId;

    @Column(name = "reviewed_by_admin_id")
    Long reviewedByAdminId;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    String rejectionReason;

    @Column(name = "more_info_reason", length = 500)
    String moreInfoReason;

    @Column(name = "member_additional_note", length = 1000)
    String memberAdditionalNote;

    @Column(name = "cancelled_at")
    Instant cancelledAt;

    @Column(name = "expired_at")
    Instant expiredAt;
}
