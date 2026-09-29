package fit.iuh.se.hsapplication.controller.nutrition;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.nutrition.DoctorDietPrescriptionService;
import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse;
import fit.iuh.se.hsnutrition.dto.UpdateDietPrescriptionRequest;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Đơn ăn uống: hội viên xem đơn của mình; bác sĩ xem và kê đơn cho hội viên trong phiên tư vấn của mình.
 * Món tra cứu được chấm màu theo đơn này (xem NutritionReferenceController).
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DietPrescriptionController {
    DietPrescriptionService prescriptions;
    DoctorDietPrescriptionService doctorPrescriptions;

    @GetMapping("/api/nutrition/diet-prescription/me")
    public ApiResponse<NutritionDietPrescriptionResponse> mine(@AuthenticationPrincipal UserAuthentication currentUser) {
        if (currentUser.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        return new ApiResponse<>(prescriptions.get(currentUser.getUserId()));
    }

    @GetMapping("/api/doctor/consultation-sessions/{sessionId}/diet-prescription")
    public ApiResponse<NutritionDietPrescriptionResponse> forSession(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long sessionId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorPrescriptions.get(currentUser.getUserId(), sessionId));
    }

    @PutMapping("/api/doctor/consultation-sessions/{sessionId}/diet-prescription")
    public ApiResponse<NutritionDietPrescriptionResponse> prescribe(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long sessionId,
            @RequestBody UpdateDietPrescriptionRequest request) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorPrescriptions.update(currentUser.getUserId(), sessionId, request));
    }

    private static void validateDoctor(UserAuthentication currentUser) {
        if (currentUser.getRole() != UserRole.DOCTOR) throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
