package fit.iuh.se.hshealthrecord.dto.workout;

import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.entity.workout.enums.TrackingMetricType;
import fit.iuh.se.hshealthrecord.entity.workout.enums.WorkoutTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWorkoutSessionRequest {

    @NotBlank(message = "{validation.exercise-code-required}")
    String exerciseCode;

    @NotBlank(message = "{validation.exercise-name-required}")
    String exerciseName;

    @NotNull(message = "{validation.exercise-category-required}")
    ExerciseCategory category;

    @NotNull(message = "{validation.tracking-type-required}")
    TrackingMetricType trackingType;

    String iconName;

    @NotNull(message = "{validation.start-time-required}")
    Instant startedAt;

    @NotNull(message = "{validation.end-time-required}")
    Instant endedAt;

    @NotNull(message = "{validation.duration-required}")
    Integer durationSeconds;

    @NotNull(message = "{validation.calories-burned-required}")
    Integer caloriesBurned;

    Integer totalCalories;

    Double distanceMeters;
    Double avgSpeedKmh;
    Integer totalSteps;
    Integer completedSets;

    WorkoutTargetType targetType;
    Double targetValue;
    String gpxTrackJson;

    @Builder.Default
    Boolean isHeartRateMonitored = false;
    Integer avgHeartRate;
    Integer maxHeartRate;

    String encodedPolyline;
    String gpxTrackUrl;
    String telemetryJsonUrl;
    String note;
}
