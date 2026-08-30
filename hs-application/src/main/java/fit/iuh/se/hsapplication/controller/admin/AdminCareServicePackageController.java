package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.CreateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.request.UpdateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.service.packageinfo.CareServicePackageService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/care-service-packages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCareServicePackageController {

    CareServicePackageService careServicePackageService;

    @GetMapping
    public ApiResponse<PageResponse<CareServicePackageResponse>> getPackages(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "status", required = false) CareServicePackageStatus status,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(careServicePackageService.getPackagesForAdmin(
                currentUser.getRole(),
                status,
                pageable
        ));
    }

    @GetMapping("/{packageId}")
    public ApiResponse<CareServicePackageResponse> getPackage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long packageId) {
        return new ApiResponse<>(careServicePackageService.getPackageForAdmin(currentUser.getRole(), packageId));
    }

    @GetMapping("/{packageId}/versions")
    public ApiResponse<List<CareServicePackageResponse>> getPackageVersions(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long packageId) {
        return new ApiResponse<>(careServicePackageService.getPackageVersionsForAdmin(
                currentUser.getRole(),
                packageId
        ));
    }

    @PostMapping
    public ApiResponse<CareServicePackageResponse> createPackage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody CreateCareServicePackageRequest request) {
        return new ApiResponse<>(careServicePackageService.createPackage(
                currentUser.getUserId(), currentUser.getRole(), request));
    }

    @PatchMapping("/{packageId}")
    public ApiResponse<CareServicePackageResponse> updatePackage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long packageId,
            @Valid @RequestBody UpdateCareServicePackageRequest request) {
        return new ApiResponse<>(careServicePackageService.updatePackage(
                currentUser.getUserId(), currentUser.getRole(), packageId, request));
    }

    @PatchMapping("/{packageId}/activate")
    public ApiResponse<CareServicePackageResponse> activatePackage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long packageId) {
        return new ApiResponse<>(careServicePackageService.activatePackage(
                currentUser.getUserId(), currentUser.getRole(), packageId));
    }

    @PatchMapping("/{packageId}/deactivate")
    public ApiResponse<CareServicePackageResponse> deactivatePackage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long packageId) {
        return new ApiResponse<>(careServicePackageService.deactivatePackage(
                currentUser.getUserId(), currentUser.getRole(), packageId));
    }

    @PatchMapping("/{packageId}/retire")
    public ApiResponse<CareServicePackageResponse> retirePackage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long packageId) {
        return new ApiResponse<>(careServicePackageService.retirePackage(
                currentUser.getUserId(), currentUser.getRole(), packageId));
    }
}
