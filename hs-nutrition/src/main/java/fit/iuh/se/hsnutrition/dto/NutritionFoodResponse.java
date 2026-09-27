package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Khớp kiểu {@code Food} của Frontend (src/types/nutrition.ts). Mọi giá trị dinh dưỡng tính trên 100 g. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionFoodResponse(
        String id,
        String group,
        String groupName,
        String foodName,
        String foodNameSpecific,
        String description,
        String guidance,
        String guidanceTitle,
        String guidanceReason,
        String cardiovascularContext,
        String afContext,
        String medicationContext,
        String sourceFoodCode,
        String sourceDescription,
        ServingReference servingReference,
        List<NutrientValue> nutrients,
        List<String> highlightNutrientCodes,
        List<EvidenceSource> evidenceSources,
        String imageUrl) {

    public record ServingReference(int amount, String unit) {}

    public record NutrientValue(
            String nutrientCode, String name, double amount, String unit,
            @JsonProperty("isKey") boolean isKey) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EvidenceSource(
            String id, String title, String sourceType, String authors, String journal,
            Integer year, String url, String summary) {}
}
