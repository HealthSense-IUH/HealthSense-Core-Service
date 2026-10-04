package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DayStepItemDto {
    String date;
    int dayNum;
    boolean isSunday;
    int steps;
    boolean isCurrent;
}
