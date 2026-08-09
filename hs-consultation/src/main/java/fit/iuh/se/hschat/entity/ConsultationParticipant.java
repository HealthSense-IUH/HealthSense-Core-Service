package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
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
        name = "consultation_participants",
        indexes = {
                @Index(name = "idx_participant_user", columnList = "user_id"),
                @Index(name = "idx_participant_session", columnList = "session_id"),
                @Index(name = "idx_participant_user_active", columnList = "user_id, active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_participant_session_user", columnNames = {"session_id", "user_id"})
        }
)
public class ConsultationParticipant extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "session_id", nullable = false)
    Long sessionId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    ConsultationParticipantRole role;

    @Column(name = "last_read_message_id", length = 100)
    String lastReadMessageId;

    @Column(name = "last_read_at")
    Instant lastReadAt;

    @Column(name = "joined_at")
    Instant joinedAt;

    @Column(name = "left_at")
    Instant leftAt;

    @Column(name = "active", nullable = false)
    Boolean active;
}
