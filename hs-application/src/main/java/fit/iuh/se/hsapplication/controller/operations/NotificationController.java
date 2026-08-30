package fit.iuh.se.hsapplication.controller.operations;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsoperations.dto.response.*;
import fit.iuh.se.hsoperations.service.NotificationService;
import fit.iuh.se.hsshared.dto.response.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> findMine(@AuthenticationPrincipal UserAuthentication user,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return new ApiResponse<>(service.findMine(user.getUserId(),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadNotificationCountResponse> unreadCount(@AuthenticationPrincipal UserAuthentication user) {
        return new ApiResponse<>(service.unreadCount(user.getUserId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markRead(@AuthenticationPrincipal UserAuthentication user,
            @PathVariable Long notificationId) {
        return new ApiResponse<>(service.markRead(user.getUserId(), notificationId));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal UserAuthentication user) {
        service.markAllRead(user.getUserId());
        return new ApiResponse<>();
    }
}
