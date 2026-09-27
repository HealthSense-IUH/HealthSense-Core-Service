package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionFoodGroupResponse(
        String id, String slug, String name, String description, String dietaryPattern,
        String icon, String imageUrl, long foodCount) {}
