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

    @NotBlank(message = "Mã bài tập không được để trống")
    String exerciseCode;

    @NotBlank(message = "Tên bài tập không được để trống")
    String exerciseName;

    @NotNull(message = "Thể loại bài tập không được để trống")
    ExerciseCategory category;

    @NotNull(message = "Dữ liệu cần ghi không được để trống")
    TrackingMetricType trackingType;

    String iconName;

    @NotNull(message = "Thời điểm bắt đầu không được để trống")
    Instant startedAt;

    @NotNull(message = "Thời điểm kết thúc không được để trống")
    Instant endedAt;

    @NotNull(message = "Thời lượng không được để trống")
    Integer durationSeconds;

    @NotNull(message = "Calo tiêu hao không được để trống")
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
