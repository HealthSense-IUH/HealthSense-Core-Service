package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsnutrition.dto.DietRuleResponse;
import fit.iuh.se.hsnutrition.dto.DietThresholdRequest;
import fit.iuh.se.hsnutrition.service.DietRuleService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ngưỡng mặc định để chấm màu thực phẩm theo đơn ăn uống. Chỉ SUPER_ADMIN / ADMIN (SecurityConfig.ADMIN_ENDPOINTS).
 * Bác sĩ vẫn chỉnh được ngưỡng riêng cho từng hội viên khi kê đơn.
 */
@RestController
@RequestMapping("/api/admin/nutrition/diet-rules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDietRuleController {
    DietRuleService rules;

    @GetMapping
    public ApiResponse<List<DietRuleResponse>> list() {
        return new ApiResponse<>(rules.list());
    }

    @PutMapping
    public ApiResponse<List<DietRuleResponse>> update(@RequestBody List<DietThresholdRequest> thresholds) {
        return new ApiResponse<>(rules.update(thresholds));
    }
}
