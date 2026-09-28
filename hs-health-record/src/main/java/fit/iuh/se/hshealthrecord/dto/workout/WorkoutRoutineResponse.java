package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkoutRoutineResponse {
    Long id;
    String name;
    Boolean hasWarmup;
    Integer warmupDurationSec;
    Boolean hasCooldown;
    Integer cooldownDurationSec;
    String itemsJson;
    Instant createdAt;
}
