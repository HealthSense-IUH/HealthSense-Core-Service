package fit.iuh.se.hshealthrecord.dto.workout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncStepDataRequest {

    @NotBlank(message = "Date is required (yyyy-MM-dd)")
    String date;

    @NotNull(message = "Total steps is required")
    Integer totalSteps;

    Double distanceMeters;
    Integer caloriesBurned;
    Integer activeMinutes;
    Integer targetSteps;
    List<HourlyStepDto> hourlySteps;
    String deviceSource;
}
