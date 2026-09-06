package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.UpdateDoctorAvailabilityRequest;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hschat.service.doctor.DoctorCareProfileService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/doctor/care-profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorCareProfileController {

    DoctorCareProfileService doctorCareProfileService;

    @GetMapping
    public ApiResponse<DoctorCareProfileResponse> getOwnProfile(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorCareProfileService.getOwnProfile(currentUser.getUserId()));
    }

    @PatchMapping("/availability")
    public ApiResponse<DoctorCareProfileResponse> updateOwnAvailability(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody UpdateDoctorAvailabilityRequest request) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorCareProfileService.updateOwnAvailability(currentUser.getUserId(), request));
    }

    private void validateDoctor(UserAuthentication currentUser) {
        if (currentUser == null || currentUser.getRole() != UserRole.DOCTOR)
            throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
