package fit.iuh.se.hsapplication.controller.member.statistics;

import fit.iuh.se.hschat.dto.response.statistics.ConsultationStatisticsResponse;
import fit.iuh.se.hschat.service.statistics.ConsultationStatisticsService;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/member/statistics/consultations")
@RequiredArgsConstructor
public class MemberConsultationStatisticsController {

    private final ConsultationStatisticsService service;

    @GetMapping
    public ApiResponse<ConsultationStatisticsResponse> getStatistics(
            @AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "DAY") StatisticsGroupBy groupBy,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        return new ApiResponse<>(service.getMemberStatistics(memberId(actor), from, to, groupBy, timezone));
    }

    private Long memberId(UserAuthentication actor) {
        if (actor == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (actor.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        return actor.getUserId();
    }
}
