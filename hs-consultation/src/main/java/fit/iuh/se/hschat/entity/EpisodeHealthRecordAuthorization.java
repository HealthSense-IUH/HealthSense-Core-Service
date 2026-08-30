package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizedByType;
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
@Table(name = "consultation_episode_health_records", indexes = {
        @Index(name = "idx_episode_hr_session", columnList = "session_id, authorized_at"),
        @Index(name = "idx_episode_hr_record", columnList = "health_record_id"),
        @Index(name = "idx_episode_hr_member", columnList = "member_id, session_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_episode_health_record", columnNames = {"session_id", "health_record_id"})
})
public class EpisodeHealthRecordAuthorization extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "session_id", nullable = false, updatable = false)
    Long sessionId;

    @Column(name = "health_record_id", nullable = false, updatable = false)
    Long healthRecordId;

    @Column(name = "member_id", nullable = false, updatable = false)
    Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "authorization_source", nullable = false, updatable = false, length = 40)
    EpisodeHealthRecordAuthorizationSource authorizationSource;

    @Column(name = "authorized_at", nullable = false, updatable = false)
    Instant authorizedAt;

    @Column(name = "authorized_by", updatable = false)
    Long authorizedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "authorized_by_type", nullable = false, updatable = false, length = 30)
    EpisodeHealthRecordAuthorizedByType authorizedByType;
}
