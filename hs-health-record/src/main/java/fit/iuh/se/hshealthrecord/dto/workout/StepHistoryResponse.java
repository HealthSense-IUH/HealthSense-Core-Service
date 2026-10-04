package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StepHistoryResponse {
    List<DayStepItemDto> items;
    int avgSteps;
    int comparisonPercentile;
    int activeWeeklyTrendPercent;
}
