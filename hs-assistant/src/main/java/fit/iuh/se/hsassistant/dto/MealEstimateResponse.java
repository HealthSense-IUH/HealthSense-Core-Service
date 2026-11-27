package fit.iuh.se.hsassistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MealEstimateResponse(
        String mealName,
        String confidence,
        double totalGrams,
        double energyKcal,
        double proteinG,
        double carbohydrateG,
        double fatTotalG,
        double fatSaturatedG,
        double sugarsG,
        double sodiumMg,
        double potassiumMg,
        String cardiacLevel,
        String cardiacNote,
        List<MealItem> items,
        List<NutritionReferenceFoodSummaryResponse> matchedFoods
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MealItem(
            String name,
            double estimatedGrams,
            double energyKcal,
            double proteinG,
            double carbohydrateG,
            double fatTotalG,
            double sodiumMg,
            double potassiumMg
    ) {}
}
