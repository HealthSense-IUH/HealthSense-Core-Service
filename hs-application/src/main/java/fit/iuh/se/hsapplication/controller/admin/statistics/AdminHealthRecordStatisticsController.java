package fit.iuh.se.hsapplication.controller.admin.statistics;

import fit.iuh.se.hshealthrecord.dto.response.statistics.HealthRecordStatisticsResponse;
import fit.iuh.se.hshealthrecord.service.statistics.HealthRecordStatisticsService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/statistics/health-records")
@RequiredArgsConstructor
public class AdminHealthRecordStatisticsController {

    private final HealthRecordStatisticsService service;

    @GetMapping
    public ApiResponse<HealthRecordStatisticsResponse> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "DAY") StatisticsGroupBy groupBy,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        return new ApiResponse<>(service.getStatistics(from, to, groupBy, timezone));
    }
}
