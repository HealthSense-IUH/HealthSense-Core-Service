package fit.iuh.se.hshealthrecord.entity.workout;

import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.entity.workout.enums.TrackingMetricType;
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
@Table(name = "workout_exercises", indexes = {
    @Index(name = "idx_workout_exercise_user", columnList = "user_id"),
    @Index(name = "idx_workout_exercise_category", columnList = "category"),
    @Index(name = "idx_workout_exercise_code", columnList = "code")
})
public class ExerciseEntity extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "user_id")
    Long userId; // null if global system exercise, specific userId if custom

    @Column(name = "code", nullable = false, length = 100)
    String code;

    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    ExerciseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type", nullable = false, length = 50)
    TrackingMetricType trackingType;

    @Column(name = "met_rate", nullable = false)
    Double metRate;

    @Column(name = "icon_name", length = 100)
    String iconName;

    @Builder.Default
    @Column(name = "is_system", nullable = false)
    Boolean isSystem = false;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;
}
