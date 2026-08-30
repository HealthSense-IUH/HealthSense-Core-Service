package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.CareHistoryEpisodeResponse;
import fit.iuh.se.hschat.service.carehistory.CareHistoryService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/care-history")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareHistoryController {

    CareHistoryService careHistoryService;

    @GetMapping
    public ApiResponse<PageResponse<CareHistoryEpisodeResponse>> getCareHistory(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(
                page - 1, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        return new ApiResponse<>(careHistoryService.getMemberHistory(currentUser.getUserId(), pageable));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<CareHistoryEpisodeResponse> getEpisode(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        return new ApiResponse<>(careHistoryService.getMemberEpisode(
                currentUser.getUserId(), sessionId));
    }
}
