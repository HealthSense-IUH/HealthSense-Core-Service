package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.service.packageinfo.CareServicePackageService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/care-service-packages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareServicePackageController {

    CareServicePackageService careServicePackageService;

    @GetMapping
    public ApiResponse<PageResponse<CareServicePackageResponse>> getActivePackages(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(careServicePackageService.getActivePackages(pageable));
    }

    @GetMapping("/{packageId}")
    public ApiResponse<CareServicePackageResponse> getActivePackage(@PathVariable Long packageId) {
        return new ApiResponse<>(careServicePackageService.getActivePackage(packageId));
    }
}
