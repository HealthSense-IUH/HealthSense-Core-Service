package fit.iuh.se.hshealthrecord.entity.workout;

import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.entity.workout.enums.TrackingMetricType;
import fit.iuh.se.hshealthrecord.entity.workout.enums.WorkoutTargetType;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "workout_sessions", indexes = {
    @Index(name = "idx_workout_session_user", columnList = "user_id"),
    @Index(name = "idx_workout_session_user_date", columnList = "user_id, started_at")
})
public class WorkoutSession extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "exercise_code", nullable = false, length = 100)
    String exerciseCode;

    @Column(name = "exercise_name", nullable = false, length = 255)
    String exerciseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    ExerciseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type", nullable = false, length = 50)
    TrackingMetricType trackingType;

    @Column(name = "icon_name", length = 100)
    String iconName;

    @Column(name = "started_at", nullable = false)
    Instant startedAt;

    @Column(name = "ended_at", nullable = false)
    Instant endedAt;

    @Column(name = "duration_seconds", nullable = false)
    Integer durationSeconds;

    @Column(name = "calories_burned", nullable = false)
    Integer caloriesBurned;

    @Column(name = "total_calories")
    Integer totalCalories;

    @Column(name = "distance_meters")
    Double distanceMeters;

    @Column(name = "avg_speed_kmh")
    Double avgSpeedKmh;

    @Column(name = "total_steps")
    Integer totalSteps;

    @Column(name = "completed_sets")
    Integer completedSets;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30)
    WorkoutTargetType targetType;

    @Column(name = "target_value")
    Double targetValue;

    @Column(name = "gpx_track_json", columnDefinition = "TEXT")
    String gpxTrackJson;

    @Builder.Default
    @Column(name = "is_heart_rate_monitored", nullable = false)
    Boolean isHeartRateMonitored = false;

    @Column(name = "avg_heart_rate")
    Integer avgHeartRate;

    @Column(name = "max_heart_rate")
    Integer maxHeartRate;

    @Column(name = "encoded_polyline", columnDefinition = "TEXT")
    String encodedPolyline;

    @Column(name = "gpx_track_url", length = 512)
    String gpxTrackUrl;

    @Column(name = "telemetry_json_url", length = 512)
    String telemetryJsonUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;
}
