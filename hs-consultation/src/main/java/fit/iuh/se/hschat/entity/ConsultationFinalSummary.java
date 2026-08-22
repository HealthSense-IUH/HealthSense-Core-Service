package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
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
        name = "consultation_final_summaries",
        indexes = {
                @Index(name = "idx_final_summary_session_status", columnList = "session_id, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_final_summary_session", columnNames = "session_id")
        }
)
public class ConsultationFinalSummary extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "session_id", nullable = false)
    Long sessionId;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    String summary;

    @Column(name = "observations", columnDefinition = "TEXT")
    String observations;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    String recommendations;

    @Column(name = "follow_up_recommendation", columnDefinition = "TEXT")
    String followUpRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    ConsultationFinalSummaryStatus status;

    @Column(name = "created_by_doctor_id", nullable = false)
    Long createdByDoctorId;

    @Column(name = "finalized_at")
    Instant finalizedAt;
}
