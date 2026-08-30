package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsoperations.dto.response.BusinessAuditEventResponse;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.BusinessAuditQueryService;
import fit.iuh.se.hsshared.dto.response.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/business-audit-events")
@RequiredArgsConstructor
public class AdminBusinessAuditController {
    private final BusinessAuditQueryService service;

    @GetMapping
    public ApiResponse<PageResponse<BusinessAuditEventResponse>> find(
            @AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) BusinessDomainType domainType,
            @RequestParam(required = false) Long domainId,
            @RequestParam(required = false) BusinessEventType eventType,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return new ApiResponse<>(service.find(actor.getRole(), domainType, domainId, eventType,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "occurredAt"))));
    }

    @GetMapping("/{eventId}")
    public ApiResponse<BusinessAuditEventResponse> get(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long eventId) {
        return new ApiResponse<>(service.get(actor.getRole(), eventId));
    }
}
