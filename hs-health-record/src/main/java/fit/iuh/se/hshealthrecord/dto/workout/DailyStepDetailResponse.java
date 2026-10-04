package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyStepDetailResponse {
    String date;
    int totalSteps;
    double distanceKm;
    int caloriesBurned;
    int targetSteps;
    String activePeriodStr;
    List<HourlyStepDto> hourlyData;
    String deviceSource;
}
