package fit.iuh.se.hshealthrecord.dto.workout;

import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.entity.workout.enums.TrackingMetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCustomExerciseRequest {

    @NotBlank(message = "Tên bài tập không được để trống")
    String name;

    @NotNull(message = "Thể loại bài tập không được để trống")
    ExerciseCategory category;

    @NotNull(message = "Dữ liệu cần ghi không được để trống")
    TrackingMetricType trackingType;

    Double metRate;

    String iconName;

    String description;
}
