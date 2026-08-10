package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationAttentionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
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
        name = "consultation_health_record_attentions",
        indexes = {
                @Index(name = "idx_attention_session_status", columnList = "session_id, status"),
                @Index(name = "idx_attention_record", columnList = "health_record_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_attention_session_record", columnNames = {"session_id", "health_record_id"})
        }
)
public class ConsultationHealthRecordAttention extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "session_id", nullable = false)
    Long sessionId;

    @Column(name = "health_record_id", nullable = false)
    Long healthRecordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    ConsultationAttentionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    ConsultationAttentionReason reason;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @Column(name = "reviewed_by_doctor_id")
    Long reviewedByDoctorId;
}
