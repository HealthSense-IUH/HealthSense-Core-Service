package fit.iuh.se.hshealthrecord.dto.workout;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStepGoalRequest {

    @NotNull(message = "Target steps is required")
    @Min(value = 500, message = "Target steps must be at least 500")
    @Max(value = 50000, message = "Target steps cannot exceed 50,000")
    Integer targetSteps;
}
