package fit.iuh.se.hshealthrecord.dto.workout;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeeklyWorkoutStatsResponse {
    String weekRangeLabel;
    Integer totalDurationSeconds;
    Integer totalCalories;
    Integer totalSessions;
    List<DailyDistributionItem> dailyDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DailyDistributionItem {
        Integer dayNumber; // 2 -> 7, 8 (CN)
        String dayLabel;   // '2', '3', '4', '5', '6', '7', 'CN'
        String dateStr;
        Integer durationSeconds;
        Integer calories;
        Boolean hasWorkout;
    }
}
