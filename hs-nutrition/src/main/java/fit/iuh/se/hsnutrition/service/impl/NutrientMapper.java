package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;
import fit.iuh.se.hsnutrition.entity.NutritionFood;

import fit.iuh.se.hsshared.i18n.LocalizedText;
import fit.iuh.se.hsshared.i18n.RequestLanguage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * Các chất Frontend hiển thị, đúng thứ tự và tên của NutrientCode trong src/types/nutrition.ts.
 * Chất nào nguồn không có (NULL) thì bỏ qua. EPA/DHA lưu theo gram nhưng hiển thị theo mg, nên nhân 1000.
 * Tên chất theo ngôn ngữ của request (header {@code lang}).
 */
final class NutrientMapper {
    private static final List<Nutrient> NUTRIENTS = List.of(
            new Nutrient("energy", "Năng lượng", "Energy", "kcal", NutritionFood::getEnergyKcal, 1, true),
            new Nutrient("protein", "Chất đạm (Protein)", "Protein", "g", NutritionFood::getProteinG, 1, true),
            new Nutrient("carbohydrate", "Carbohydrate", "Carbohydrate", "g", NutritionFood::getCarbohydrateG, 1, true),
            new Nutrient("fiber", "Chất xơ tiêu hóa", "Dietary fiber", "g", NutritionFood::getFiberG, 1, false),
            new Nutrient("fiber_crude", "Chất xơ thô (celluloza)", "Crude fiber (cellulose)", "g", NutritionFood::getFiberCrudeG, 1, false),
            new Nutrient("sugars", "Đường tổng", "Total sugars", "g", NutritionFood::getSugarsG, 1, false),
            new Nutrient("fat_total", "Tổng chất béo", "Total fat", "g", NutritionFood::getFatTotalG, 1, true),
            new Nutrient("fat_saturated", "Chất béo bão hòa", "Saturated fat", "g", NutritionFood::getFatSaturatedG, 1, true),
            new Nutrient("fat_monounsaturated", "Chất béo không bão hòa đơn", "Monounsaturated fat", "g", NutritionFood::getFatMonounsaturatedG, 1, false),
            new Nutrient("fat_polyunsaturated", "Chất béo không bão hòa đa", "Polyunsaturated fat", "g", NutritionFood::getFatPolyunsaturatedG, 1, false),
            new Nutrient("cholesterol", "Cholesterol", "Cholesterol", "mg", NutritionFood::getCholesterolMg, 1, false),
            new Nutrient("sodium", "Natri (Sodium)", "Sodium", "mg", NutritionFood::getSodiumMg, 1, true),
            new Nutrient("potassium", "Kali (Potassium)", "Potassium", "mg", NutritionFood::getPotassiumMg, 1, true),
            new Nutrient("magnesium", "Magie (Magnesium)", "Magnesium", "mg", NutritionFood::getMagnesiumMg, 1, true),
            new Nutrient("caffeine", "Caffeine", "Caffeine", "mg", NutritionFood::getCaffeineMg, 1, false),
            new Nutrient("alcohol", "Cồn (Alcohol)", "Alcohol", "g", NutritionFood::getAlcoholG, 1, false),
            new Nutrient("vitamin_k", "Vitamin K", "Vitamin K", "µg", NutritionFood::getVitaminKMcg, 1, false),
            new Nutrient("epa", "Omega-3 EPA", "Omega-3 EPA", "mg", NutritionFood::getEpaG, 1000, false),
            new Nutrient("dha", "Omega-3 DHA", "Omega-3 DHA", "mg", NutritionFood::getDhaG, 1000, false));

    private NutrientMapper() {}

    static List<NutrientValue> of(NutritionFood food) {
        Locale locale = RequestLanguage.current();
        return NUTRIENTS.stream().map(nutrient -> nutrient.valueOf(food, locale)).filter(Objects::nonNull).toList();
    }

    static Double amount(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().doubleValue();
    }

    private record Nutrient(String code, String name, String nameEn, String unit,
                            Function<NutritionFood, BigDecimal> column, int multiplier, boolean key) {
        NutrientValue valueOf(NutritionFood food, Locale locale) {
            BigDecimal value = column.apply(food);
            if (value == null) return null;
            return new NutrientValue(code, LocalizedText.pick(name, nameEn, locale),
                    amount(value.multiply(BigDecimal.valueOf(multiplier))), unit, key);
        }
    }
}
