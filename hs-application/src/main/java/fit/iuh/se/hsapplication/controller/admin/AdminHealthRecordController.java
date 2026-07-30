package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hshealthrecord.dto.request.AdminCreateHealthRecordRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.service.HealthRecordService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/health-records")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminHealthRecordController {

    HealthRecordService healthRecordService;

    @PostMapping
    public ApiResponse<HealthRecordResponse> createRecordForMember(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestBody @Valid AdminCreateHealthRecordRequest request) {
        return new ApiResponse<>(healthRecordService.createRecordForMember(currentUser.getUserId(), request));
    }
}
