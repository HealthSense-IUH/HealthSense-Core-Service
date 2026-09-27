package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Một dòng trong danh sách tra cứu. Giá trị tính trên 100 g phần ăn được.
 * {@code name} là tên tiếng Anh; {@code nameVi} chỉ có với nguồn VN_FCT (và món USDA đã được dịch).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionReferenceFoodSummaryResponse(
        String id, String source, String sourceFoodCode, String name, String nameVi, String category,
        Double energyKcal, Double proteinG, Double carbohydrateG, Double fatTotalG) {}
