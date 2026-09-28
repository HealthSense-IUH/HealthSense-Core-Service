package fit.iuh.se.hshealthrecord.dto.workout;

import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.entity.workout.enums.TrackingMetricType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseResponse {
    Long id;
    String code;
    String name;
    ExerciseCategory category;
    TrackingMetricType trackingType;
    Double metRate;
    String iconName;
    Boolean isSystem;
    Boolean isFavorite;
    String description;
    Instant createdAt;
}
