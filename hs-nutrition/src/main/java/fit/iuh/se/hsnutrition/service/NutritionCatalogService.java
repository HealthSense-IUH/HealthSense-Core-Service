package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.dto.NutritionFoodGroupResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse;

import java.util.List;

public interface NutritionCatalogService {
    List<NutritionFoodGroupResponse> getGroups();

    NutritionFoodGroupResponse getGroup(String idOrSlug);

    List<NutritionFoodResponse> getGroupFoods(String idOrSlug);

    NutritionFoodResponse getFood(String id);

    /** Không phân biệt hoa thường và dấu tiếng Việt ("ca hoi" tìm được "Cá hồi"). */
    List<NutritionFoodResponse> searchFoods(String query);
}
