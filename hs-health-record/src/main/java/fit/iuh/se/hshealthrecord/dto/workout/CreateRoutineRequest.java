package fit.iuh.se.hshealthrecord.dto.workout;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateRoutineRequest {

    @NotBlank(message = "{validation.routine-name-required}")
    String name;

    @Builder.Default
    Boolean hasWarmup = false;

    Integer warmupDurationSec;

    @Builder.Default
    Boolean hasCooldown = false;

    Integer cooldownDurationSec;

    String itemsJson;
}
