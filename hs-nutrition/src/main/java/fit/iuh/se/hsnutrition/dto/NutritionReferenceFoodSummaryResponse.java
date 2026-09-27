package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Một dòng trong danh sách tra cứu. Giá trị tính trên 100 g. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionReferenceFoodSummaryResponse(
        String id, String sourceFoodCode, String name, String category,
        Double energyKcal, Double proteinG, Double carbohydrateG, Double fatTotalG) {}
