package fit.iuh.se.hsapplication.controller.nutrition;

import fit.iuh.se.hsnutrition.dto.NutritionFoodGroupResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse;
import fit.iuh.se.hsnutrition.service.NutritionCatalogService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Danh mục dinh dưỡng chỉ đọc; mọi người dùng đã đăng nhập đều xem được. */
@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {
    private final NutritionCatalogService catalog;

    @GetMapping("/groups")
    public ApiResponse<List<NutritionFoodGroupResponse>> groups() {
        return new ApiResponse<>(catalog.getGroups());
    }

    @GetMapping("/groups/{idOrSlug}")
    public ApiResponse<NutritionFoodGroupResponse> group(@PathVariable String idOrSlug) {
        return new ApiResponse<>(catalog.getGroup(idOrSlug));
    }

    @GetMapping("/groups/{idOrSlug}/foods")
    public ApiResponse<List<NutritionFoodResponse>> groupFoods(@PathVariable String idOrSlug) {
        return new ApiResponse<>(catalog.getGroupFoods(idOrSlug));
    }

    @GetMapping("/foods/search")
    public ApiResponse<List<NutritionFoodResponse>> search(@RequestParam(defaultValue = "") String q) {
        return new ApiResponse<>(catalog.searchFoods(q));
    }

    @GetMapping("/foods/{id}")
    public ApiResponse<NutritionFoodResponse> food(@PathVariable String id) {
        return new ApiResponse<>(catalog.getFood(id));
    }
}
