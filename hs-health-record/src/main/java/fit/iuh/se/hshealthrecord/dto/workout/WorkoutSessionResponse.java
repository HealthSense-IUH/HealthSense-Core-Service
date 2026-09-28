package fit.iuh.se.hshealthrecord.dto.workout;

import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.entity.workout.enums.TrackingMetricType;
import fit.iuh.se.hshealthrecord.entity.workout.enums.WorkoutTargetType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkoutSessionResponse {
    Long id;
    String exerciseCode;
    String exerciseName;
    ExerciseCategory category;
    TrackingMetricType trackingType;
    String iconName;
    Instant startedAt;
    Instant endedAt;
    Integer durationSeconds;
    Integer caloriesBurned;
    Integer totalCalories;
    Double distanceMeters;
    Double avgSpeedKmh;
    Integer totalSteps;
    Integer completedSets;
    WorkoutTargetType targetType;
    Double targetValue;
    String gpxTrackJson;
    Boolean isHeartRateMonitored;
    Integer avgHeartRate;
    Integer maxHeartRate;
    String encodedPolyline;
    String gpxTrackUrl;
    String telemetryJsonUrl;
    String note;
    Instant createdAt;
}
