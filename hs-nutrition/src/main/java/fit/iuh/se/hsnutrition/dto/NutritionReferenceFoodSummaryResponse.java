package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Một dòng trong danh sách tra cứu. Giá trị tính trên 100 g phần ăn được.
 * Quy ước tên: nguồn USDA -> displayName = tên gốc tiếng Anh, localName = tên dịch tiếng Việt;
 * nguồn VN_FCT -> displayName = localName = tên tiếng Việt.
 * {@code group}, {@code groupName}: nhóm chung (nutrition_food_groups).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionReferenceFoodSummaryResponse(
        String id, String source, String sourceFoodCode, String displayName, String localName,
        String group, String groupName,
        Double energyKcal, Double proteinG, Double carbohydrateG, Double fatTotalG) {}
