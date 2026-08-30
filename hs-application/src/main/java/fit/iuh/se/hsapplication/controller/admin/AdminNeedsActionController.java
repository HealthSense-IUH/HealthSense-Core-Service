package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsoperations.dto.request.ResolveNeedsActionRequest;
import fit.iuh.se.hsoperations.dto.response.NeedsActionResponse;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.NeedsActionService;
import fit.iuh.se.hsshared.dto.response.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/needs-actions")
@RequiredArgsConstructor
public class AdminNeedsActionController {
    private final NeedsActionService service;

    @GetMapping
    public ApiResponse<PageResponse<NeedsActionResponse>> find(@AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) NeedsActionStatus status,
            @RequestParam(required = false) NeedsActionType type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return new ApiResponse<>(service.find(actor.getRole(), status, type,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{itemId}")
    public ApiResponse<NeedsActionResponse> get(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long itemId) {
        return new ApiResponse<>(service.get(actor.getRole(), itemId));
    }

    @PatchMapping("/{itemId}/claim")
    public ApiResponse<NeedsActionResponse> claim(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long itemId) {
        return new ApiResponse<>(service.claim(actor.getRole(), actor.getUserId(), itemId));
    }

    @PatchMapping("/{itemId}/resolve")
    public ApiResponse<NeedsActionResponse> resolve(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long itemId, @Valid @RequestBody ResolveNeedsActionRequest request) {
        return new ApiResponse<>(service.resolve(actor.getRole(), actor.getUserId(), itemId, request.resolution()));
    }
}
