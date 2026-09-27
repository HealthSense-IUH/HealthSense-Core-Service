package fit.iuh.se.hsapplication.controller.nutrition;

import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.service.NutritionReferenceService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Tra cứu cơ sở dữ liệu dinh dưỡng tham chiếu (USDA FNDDS + Bảng TPTP Việt Nam 2007); chỉ đọc, mọi người dùng đã đăng nhập. */
@RestController
@RequestMapping("/api/nutrition/reference")
@RequiredArgsConstructor
public class NutritionReferenceController {
    private final NutritionReferenceService reference;

    @GetMapping("/foods")
    public ApiResponse<PageResponse<NutritionReferenceFoodSummaryResponse>> foods(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new ApiResponse<>(reference.searchFoods(q, group, source, page, size));
    }

    @GetMapping("/foods/{id}")
    public ApiResponse<NutritionReferenceFoodResponse> food(@PathVariable String id) {
        return new ApiResponse<>(reference.getFood(id));
    }
}
