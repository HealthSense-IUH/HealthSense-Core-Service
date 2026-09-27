package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;

/** Tra cứu toàn bộ cơ sở dữ liệu dinh dưỡng tham chiếu (bảng nutrition_foods: USDA FNDDS + Bảng TPTP Việt Nam 2007). */
public interface NutritionReferenceService {
    String SOURCE_USDA = "USDA_FNDDS";
    String SOURCE_VIETNAM = "VN_FCT";

    /**
     * Không có {@code query}: duyệt theo tên. Có {@code query}: tìm theo tên tiếng Anh và tên tiếng Việt,
     * khớp tiền tố, không phân biệt dấu ("rau muong" ra "Rau muống"). {@code group}: null, id hoặc slug của
     * nhóm chung (danh sách và số món ở /api/nutrition/groups). {@code source}: null, USDA_FNDDS hoặc VN_FCT.
     * {@code page} bắt đầu từ 1.
     */
    PageResponse<NutritionReferenceFoodSummaryResponse> searchFoods(String query, String group, String source,
                                                                    int page, int size);

    NutritionReferenceFoodResponse getFood(String id);
}
