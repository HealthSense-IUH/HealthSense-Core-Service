package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.DoctorCareProfileRequest;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hschat.service.doctor.DoctorCareProfileService;
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
@RequestMapping("/api/admin/doctors/{doctorId}/care-profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDoctorCareProfileController {

    DoctorCareProfileService doctorCareProfileService;

    @GetMapping
    public ApiResponse<DoctorCareProfileResponse> getProfile(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long doctorId) {
        return new ApiResponse<>(doctorCareProfileService.getProfile(
                currentUser.getUserId(),
                currentUser.getRole(),
                doctorId
        ));
    }

    @PutMapping
    public ApiResponse<DoctorCareProfileResponse> upsertProfile(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorCareProfileRequest request) {
        return new ApiResponse<>(doctorCareProfileService.upsertProfile(
                currentUser.getUserId(),
                currentUser.getRole(),
                doctorId,
                request
        ));
    }
}
