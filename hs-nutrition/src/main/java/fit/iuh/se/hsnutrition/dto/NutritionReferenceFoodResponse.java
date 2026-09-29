package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;

import java.util.List;

/**
 * Chi tiết một thực phẩm trong cơ sở dữ liệu tham chiếu (số liệu nguồn, không phải khuyến nghị lâm sàng).
 * Tên theo cùng quy ước với {@link NutritionReferenceFoodSummaryResponse}.
 * {@code group}, {@code groupName}: nhóm chung; {@code sourceCategory}: phân loại gốc của nguồn
 * (USDA: nhóm WWEIA tiếng Anh; Việt Nam: nhóm của sách).
 * {@code wastePct}: tỉ lệ thải bỏ khi sơ chế (%), chỉ nguồn VN_FCT có.
 * {@code advice}: màu và lý do theo đơn ăn uống của hội viên đang xem; chỉ có khi người xem là hội viên.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionReferenceFoodResponse(
        String id, String sourceFoodCode, String displayName, String localName, String group, String groupName,
        String sourceCategory, String source, String sourceVersion, Double wastePct, List<NutrientValue> nutrients,
        List<Portion> portions, DietAdvice advice) {

    public record Portion(String description, double gramWeight, @JsonProperty("isDefault") boolean isDefault) {}
}
