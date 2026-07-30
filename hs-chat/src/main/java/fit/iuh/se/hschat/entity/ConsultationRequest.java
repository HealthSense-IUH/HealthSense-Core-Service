package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

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
                @Index(name = "idx_request_health_record", columnList = "health_record_id")
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

    @Column(name = "reason", nullable = false, length = 1000)
    String reason;

    @Column(name = "preferred_doctor_id")
    Long preferredDoctorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    ConsultationRequestStatus status;

    @Column(name = "assigned_doctor_id")
    Long assignedDoctorId;

    @Column(name = "consultation_session_id", unique = true)
    Long consultationSessionId;

    @Column(name = "reviewed_by_admin_id")
    Long reviewedByAdminId;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    String rejectionReason;
}
