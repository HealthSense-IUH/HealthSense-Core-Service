package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.DoctorConsultationOfferResponse;
import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor/consultation-offers")
@RequiredArgsConstructor
public class DoctorConsultationOfferController {
    private final DoctorOfferService offerService;

    @GetMapping("/current")
    public ApiResponse<DoctorConsultationOfferResponse> current(@AuthenticationPrincipal UserAuthentication user) {
        validateDoctor(user);
        return new ApiResponse<>(offerService.getCurrentOffer(user.getUserId()));
    }

    @PostMapping("/{offerId}/accept")
    public ApiResponse<DoctorConsultationOfferResponse> accept(@AuthenticationPrincipal UserAuthentication user,
            @PathVariable String offerId) {
        validateDoctor(user);
        return new ApiResponse<>(offerService.accept(user.getUserId(), offerId));
    }

    @PostMapping("/{offerId}/reject")
    public ApiResponse<DoctorConsultationOfferResponse> reject(@AuthenticationPrincipal UserAuthentication user,
            @PathVariable String offerId) {
        validateDoctor(user);
        return new ApiResponse<>(offerService.reject(user.getUserId(), offerId));
    }

    private void validateDoctor(UserAuthentication user) {
        if (user == null || user.getRole() != UserRole.DOCTOR) throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
