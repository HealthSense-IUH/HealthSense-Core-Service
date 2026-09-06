package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchPreferencesRequest;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchStatusRequest;
import fit.iuh.se.hschat.dto.response.DoctorDispatchStatusResponse;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchStatusService;
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
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorDispatchController {

    DoctorDispatchStatusService dispatchStatusService;

    @GetMapping("/dispatch-status")
    public ApiResponse<DoctorDispatchStatusResponse> getStatus(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        validateDoctor(currentUser);
        return new ApiResponse<>(dispatchStatusService.getStatus(currentUser.getUserId()));
    }

    @PatchMapping("/dispatch-status")
    public ApiResponse<DoctorDispatchStatusResponse> updateStatus(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody UpdateDoctorDispatchStatusRequest request) {
        validateDoctor(currentUser);
        return new ApiResponse<>(dispatchStatusService.updateStatus(currentUser.getUserId(), request));
    }

    @PatchMapping("/dispatch-preferences")
    public ApiResponse<DoctorDispatchStatusResponse> updatePreferences(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody UpdateDoctorDispatchPreferencesRequest request) {
        validateDoctor(currentUser);
        return new ApiResponse<>(dispatchStatusService.updatePreferences(currentUser.getUserId(), request));
    }

    private void validateDoctor(UserAuthentication currentUser) {
        if (currentUser == null || currentUser.getRole() != UserRole.DOCTOR)
            throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
