package fit.iuh.se.hshealthrecord.entity.workout;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "workout_routines", indexes = {
    @Index(name = "idx_workout_routine_user", columnList = "user_id")
})
public class WorkoutRoutine extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Builder.Default
    @Column(name = "has_warmup", nullable = false)
    Boolean hasWarmup = false;

    @Column(name = "warmup_duration_sec")
    Integer warmupDurationSec;

    @Builder.Default
    @Column(name = "has_cooldown", nullable = false)
    Boolean hasCooldown = false;

    @Column(name = "cooldown_duration_sec")
    Integer cooldownDurationSec;

    @Column(name = "items_json", columnDefinition = "TEXT")
    String itemsJson;
}
