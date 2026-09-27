package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;
import fit.iuh.se.hsnutrition.entity.NutritionFood;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 18 chất Frontend hiển thị, đúng thứ tự và tên của NutrientCode trong src/types/nutrition.ts.
 * EPA/DHA lưu theo gram (FNDDS) nhưng hiển thị theo mg, nên nhân 1000.
 */
final class NutrientMapper {
    private static final List<Nutrient> NUTRIENTS = List.of(
            new Nutrient("energy", "Năng lượng", "kcal", NutritionFood::getEnergyKcal, 1, true),
            new Nutrient("protein", "Chất đạm (Protein)", "g", NutritionFood::getProteinG, 1, true),
            new Nutrient("carbohydrate", "Carbohydrate", "g", NutritionFood::getCarbohydrateG, 1, true),
            new Nutrient("fiber", "Chất xơ tiêu hóa", "g", NutritionFood::getFiberG, 1, false),
            new Nutrient("sugars", "Đường tổng", "g", NutritionFood::getSugarsG, 1, false),
            new Nutrient("fat_total", "Tổng chất béo", "g", NutritionFood::getFatTotalG, 1, true),
            new Nutrient("fat_saturated", "Chất béo bão hòa", "g", NutritionFood::getFatSaturatedG, 1, true),
            new Nutrient("fat_monounsaturated", "Chất béo không bão hòa đơn", "g", NutritionFood::getFatMonounsaturatedG, 1, false),
            new Nutrient("fat_polyunsaturated", "Chất béo không bão hòa đa", "g", NutritionFood::getFatPolyunsaturatedG, 1, false),
            new Nutrient("cholesterol", "Cholesterol", "mg", NutritionFood::getCholesterolMg, 1, false),
            new Nutrient("sodium", "Natri (Sodium)", "mg", NutritionFood::getSodiumMg, 1, true),
            new Nutrient("potassium", "Kali (Potassium)", "mg", NutritionFood::getPotassiumMg, 1, true),
            new Nutrient("magnesium", "Magie (Magnesium)", "mg", NutritionFood::getMagnesiumMg, 1, true),
            new Nutrient("caffeine", "Caffeine", "mg", NutritionFood::getCaffeineMg, 1, false),
            new Nutrient("alcohol", "Cồn (Alcohol)", "g", NutritionFood::getAlcoholG, 1, false),
            new Nutrient("vitamin_k", "Vitamin K", "µg", NutritionFood::getVitaminKMcg, 1, false),
            new Nutrient("epa", "Omega-3 EPA", "mg", NutritionFood::getEpaG, 1000, false),
            new Nutrient("dha", "Omega-3 DHA", "mg", NutritionFood::getDhaG, 1000, false));

    private NutrientMapper() {}

    static List<NutrientValue> of(NutritionFood food) {
        return NUTRIENTS.stream().map(nutrient -> nutrient.valueOf(food)).filter(Objects::nonNull).toList();
    }

    static Double amount(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().doubleValue();
    }

    private record Nutrient(String code, String name, String unit, Function<NutritionFood, BigDecimal> column,
                            int multiplier, boolean key) {
        NutrientValue valueOf(NutritionFood food) {
            BigDecimal value = column.apply(food);
            if (value == null) return null;
            return new NutrientValue(code, name, amount(value.multiply(BigDecimal.valueOf(multiplier))), unit, key);
        }
    }
}
