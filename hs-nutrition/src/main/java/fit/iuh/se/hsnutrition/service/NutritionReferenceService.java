package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.dto.NutritionReferenceCategoryResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;

import java.util.List;

/** Tra cứu toàn bộ cơ sở dữ liệu dinh dưỡng tham chiếu (USDA FNDDS, bảng nutrition_foods). */
public interface NutritionReferenceService {
    /**
     * Không có {@code query}: duyệt theo tên. Có {@code query}: tìm theo từ khóa tiếng Anh, khớp tiền tố,
     * bỏ dấu tiếng Việt trước khi tìm ("phở" tìm như "pho"). {@code page} bắt đầu từ 1.
     */
    PageResponse<NutritionReferenceFoodSummaryResponse> searchFoods(String query, String category, int page, int size);

    NutritionReferenceFoodResponse getFood(String id);

    List<NutritionReferenceCategoryResponse> getCategories();
}
