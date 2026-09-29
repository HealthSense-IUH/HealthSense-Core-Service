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

    @NotBlank(message = "{validation.exercise-name-required}")
    String name;

    @NotNull(message = "{validation.exercise-category-required}")
    ExerciseCategory category;

    @NotNull(message = "{validation.tracking-type-required}")
    TrackingMetricType trackingType;

    Double metRate;

    String iconName;

    String description;
}
