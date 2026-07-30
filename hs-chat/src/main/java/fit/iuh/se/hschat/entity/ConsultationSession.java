package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    ConsultationSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    ConsultationStatus status;

    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "ends_at", nullable = false)
    Instant endsAt;

    @Column(name = "support_ends_at")
    Instant supportEndsAt;

    @Column(name = "closed_at")
    Instant closedAt;

    @Column(name = "close_reason", length = 500)
    String closeReason;

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
