package fit.iuh.se.hsapplication.controller.admin.statistics;

import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsuser.dto.response.statistics.UserStatisticsResponse;
import fit.iuh.se.hsuser.service.statistics.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/statistics/users")
@RequiredArgsConstructor
public class AdminUserStatisticsController {

    private final UserStatisticsService service;

    @GetMapping
    public ApiResponse<UserStatisticsResponse> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "DAY") StatisticsGroupBy groupBy,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        return new ApiResponse<>(service.getStatistics(from, to, groupBy, timezone));
    }
}
