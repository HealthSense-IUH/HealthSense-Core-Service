package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationCompletionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import fit.iuh.se.hschat.entity.enums.CareOperationalReviewReason;
import fit.iuh.se.hschat.entity.enums.CareTerminationReason;
import fit.iuh.se.hsuser.entity.enums.UserRole;
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
@Table(
        name = "consultation_sessions",
        indexes = {
                @Index(name = "idx_session_member_status", columnList = "member_id, status"),
                @Index(name = "idx_session_doctor_status", columnList = "doctor_id, status"),
                @Index(name = "idx_session_status_ends_at", columnList = "status, ends_at"),
                @Index(name = "idx_session_member_last_message", columnList = "member_id, last_message_at"),
                @Index(name = "idx_session_doctor_last_message", columnList = "doctor_id, last_message_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_session_request_id", columnNames = "request_id")
        }
)
public class ConsultationSession extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "member_id", nullable = false)
    Long memberId;

    @Column(name = "doctor_id", nullable = false)
    Long doctorId;

    @Column(name = "created_by_admin_id")
    Long createdByAdminId;

    @Column(name = "exceptional_override", nullable = false)
    @Builder.Default
    Boolean exceptionalOverride = false;

    @Column(name = "override_reason", length = 1000)
    String overrideReason;

    @Column(name = "override_service_scope", length = 2000)
    String overrideServiceScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    ConsultationSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    ConsultationStatus status;

    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "activated_at")
    Instant activatedAt;

    @Column(name = "ends_at", nullable = false)
    Instant endsAt;

    @Column(name = "support_ends_at")
    Instant supportEndsAt;

    @Column(name = "support_schedule_snapshot_json", columnDefinition = "TEXT")
    String supportScheduleSnapshotJson;

    @Column(name = "support_timezone_snapshot", length = 80)
    String supportTimezoneSnapshot;

    @Column(name = "package_id")
    Long packageId;

    @Column(name = "package_version")
    Integer packageVersion;

    @Column(name = "package_price_snapshot", precision = 14, scale = 2)
    BigDecimal packagePriceSnapshot;

    @Column(name = "package_duration_days_snapshot")
    Integer packageDurationDaysSnapshot;

    @Column(name = "completed_at")
    Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_closure_status", length = 40)
    FinalSummaryClosureStatus summaryClosureStatus;

    @Column(name = "summary_due_at")
    Instant summaryDueAt;

    @Column(name = "summary_escalated_at")
    Instant summaryEscalatedAt;

    @Column(name = "summary_escalation_reason", length = 1000)
    String summaryEscalationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_reason", length = 40)
    ConsultationCompletionReason completionReason;

    @Column(name = "closed_at")
    Instant closedAt;

    @Column(name = "close_reason", length = 500)
    String closeReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason", length = 50)
    CareTerminationReason terminationReason;

    @Column(name = "termination_requested_by")
    Long terminationRequestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_requested_by_role", length = 30)
    UserRole terminationRequestedByRole;

    @Column(name = "termination_requested_at")
    Instant terminationRequestedAt;

    @Column(name = "termination_decided_by")
    Long terminationDecidedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_decided_by_role", length = 30)
    UserRole terminationDecidedByRole;

    @Column(name = "termination_decided_at")
    Instant terminationDecidedAt;

    @Column(name = "meaningful_care_occurred", nullable = false)
    @Builder.Default
    Boolean meaningfulCareOccurred = false;

    @Column(name = "operational_review_required", nullable = false)
    @Builder.Default
    Boolean operationalReviewRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_review_reason", length = 50)
    CareOperationalReviewReason operationalReviewReason;

    @Column(name = "operational_review_flagged_at")
    Instant operationalReviewFlaggedAt;

    @Column(name = "health_record_id")
    Long healthRecordId;

    @Column(name = "request_id", unique = true)
    Long requestId;

    @Column(name = "last_message_id", length = 100)
    String lastMessageId;

    @Column(name = "last_message_preview", length = 500)
    String lastMessagePreview;

    @Column(name = "last_message_at")
    Instant lastMessageAt;
}
