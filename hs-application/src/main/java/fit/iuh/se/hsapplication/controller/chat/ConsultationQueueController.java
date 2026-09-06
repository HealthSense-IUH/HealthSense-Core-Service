package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.ConsultationQueueStatisticsResponse;
import fit.iuh.se.hschat.service.request.ConsultationRequestService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultation-queue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationQueueController {

    ConsultationRequestService consultationRequestService;

    @GetMapping("/statistics")
    public ApiResponse<ConsultationQueueStatisticsResponse> getStatistics(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        return new ApiResponse<>(consultationRequestService.getQueueStatistics(currentUser.getUserId()));
    }
}
