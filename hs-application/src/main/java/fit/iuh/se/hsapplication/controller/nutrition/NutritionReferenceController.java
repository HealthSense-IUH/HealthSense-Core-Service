package fit.iuh.se.hsapplication.controller.nutrition;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.NutritionReferenceService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Tra cứu cơ sở dữ liệu dinh dưỡng tham chiếu (USDA FNDDS + Bảng TPTP Việt Nam 2007); chỉ đọc, mọi người dùng đã
 * đăng nhập. Người xem là hội viên thì mỗi món được chấm màu theo đơn ăn uống của họ.
 */
@RestController
@RequestMapping("/api/nutrition/reference")
@RequiredArgsConstructor
public class NutritionReferenceController {
    private final NutritionReferenceService reference;
    private final DietPrescriptionService prescriptions;

    @GetMapping("/foods")
    public ApiResponse<PageResponse<NutritionReferenceFoodSummaryResponse>> foods(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new ApiResponse<>(reference.searchFoods(q, group, source, page, size, dietOf(currentUser)));
    }

    @GetMapping("/foods/{id}")
    public ApiResponse<NutritionReferenceFoodResponse> food(@AuthenticationPrincipal UserAuthentication currentUser,
                                                            @PathVariable String id) {
        return new ApiResponse<>(reference.getFood(id, dietOf(currentUser)));
    }

    /** Chỉ hội viên có đơn ăn uống; bác sĩ và quản trị xem số liệu thuần. */
    private DietProfile dietOf(UserAuthentication currentUser) {
        return currentUser != null && currentUser.getRole() == UserRole.MEMBER
                ? prescriptions.profileOf(currentUser.getUserId()) : null;
    }
}
