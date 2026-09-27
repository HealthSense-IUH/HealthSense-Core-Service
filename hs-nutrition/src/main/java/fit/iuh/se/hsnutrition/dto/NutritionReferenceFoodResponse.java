package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;

import java.util.List;

/**
 * Chi tiết một thực phẩm trong cơ sở dữ liệu tham chiếu. Chỉ có số liệu, không kèm khuyến nghị.
 * {@code wastePct}: tỉ lệ thải bỏ khi sơ chế (%), chỉ nguồn VN_FCT có.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionReferenceFoodResponse(
        String id, String sourceFoodCode, String name, String nameVi, String category, String source,
        String sourceVersion, Double wastePct, List<NutrientValue> nutrients, List<Portion> portions) {

    public record Portion(String description, double gramWeight, @JsonProperty("isDefault") boolean isDefault) {}
}
