package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HourlyStepDto {
    int hour;
    int steps;
}
