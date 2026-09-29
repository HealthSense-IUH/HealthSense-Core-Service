package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyActivityResponse {
    String date;
    Integer totalSteps;
    Integer targetSteps;
    Integer activeMinutes;
    Integer targetActiveMinutes;
    Integer caloriesBurned;
    Integer targetCalories;
    Integer cardioLoadScore;
    String cardioLoadStatus; // 'RECOVERY', 'OPTIMAL', 'OVERTRAINING'
}
