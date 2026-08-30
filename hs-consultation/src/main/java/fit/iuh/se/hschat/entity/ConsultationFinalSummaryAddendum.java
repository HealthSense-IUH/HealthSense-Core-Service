package fit.iuh.se.hschat.entity;

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
@org.hibernate.annotations.Immutable
@Table(name = "consultation_final_summary_addenda", indexes = {
        @Index(name = "idx_summary_addendum_summary_created", columnList = "summary_id, created_at")
})
public class ConsultationFinalSummaryAddendum extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "summary_id", nullable = false, updatable = false)
    Long summaryId;

    @Column(name = "session_id", nullable = false, updatable = false)
    Long sessionId;

    @Column(name = "author_doctor_id", nullable = false, updatable = false)
    Long authorDoctorId;

    @Column(name = "reason", nullable = false, updatable = false, length = 1000)
    String reason;

    @Column(name = "content", nullable = false, updatable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "authored_at", nullable = false, updatable = false)
    Instant authoredAt;
}
