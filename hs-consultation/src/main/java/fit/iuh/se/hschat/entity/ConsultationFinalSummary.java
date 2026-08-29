package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    Long version = 0L;

    @Column(name = "session_id", nullable = false)
    Long sessionId;

    @Column(name = "summary", columnDefinition = "TEXT")
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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "consultation_final_summary_health_records",
            joinColumns = @JoinColumn(name = "summary_id")
    )
    @Column(name = "health_record_id", nullable = false)
    @Builder.Default
    Set<Long> referencedHealthRecordIds = new LinkedHashSet<>();
}
