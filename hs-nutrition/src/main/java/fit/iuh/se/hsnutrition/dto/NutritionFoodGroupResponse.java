package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Nhóm thực phẩm chung cho mọi nguồn.
 * {@code foodCount}: số thực phẩm của nhóm trong cơ sở dữ liệu tham chiếu; {@code sourceCounts}: tách theo nguồn
 * (VN_FCT, USDA_FNDDS; nguồn không có món nào thì không có khóa); {@code guidanceFoodCount}: số món có khuyến nghị.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionFoodGroupResponse(
        String id, String slug, String name, String description, String icon, String imageUrl,
        long foodCount, Map<String, Long> sourceCounts, long guidanceFoodCount) {}
