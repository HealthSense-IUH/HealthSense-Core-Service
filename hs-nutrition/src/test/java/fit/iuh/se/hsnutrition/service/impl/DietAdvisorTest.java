package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.DietAdvice;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
import fit.iuh.se.hsnutrition.entity.NutritionFoodGroup;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.DietThreshold;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DietAdvisorTest {

    private static Map<DietRuleCode, DietThreshold> defaultThresholds() {
        return Map.of(
                DietRuleCode.ALCOHOL, new DietThreshold(0.0, null, null),
                DietRuleCode.CAFFEINE, new DietThreshold(null, 50.0, null),
                DietRuleCode.SUGARS, new DietThreshold(null, 15.0, null),
                DietRuleCode.SODIUM, new DietThreshold(500.0, 200.0, null),
                DietRuleCode.SATURATED_FAT, new DietThreshold(5.0, 2.0, null),
                DietRuleCode.VITAMIN_K, new DietThreshold(100.0, 40.0, null),
                DietRuleCode.NA_K_RATIO, new DietThreshold(1.5, null, 0.5),
                DietRuleCode.MAGNESIUM, new DietThreshold(null, null, 40.0)
        );
    }

    private static NutritionFood foodWithGroup(String groupId) {
        NutritionFoodGroup group = new NutritionFoodGroup();
        ReflectionTestUtils.setField(group, "id", groupId);
        NutritionFood food = new NutritionFood();
        ReflectionTestUtils.setField(food, "group", group);
        return food;
    }

    @Test
    void returnsNullWhenProfileHasNoActiveRules() {
        NutritionFood food = foodWithGroup("GRAIN");
        DietProfile general = DietProfile.general(defaultThresholds());

        assertThat(DietAdvisor.advise(food, general, Locale.ENGLISH)).isNull();
    }

    @Test
    void returnsLimitForAlcoholAndHighSodium() {
        NutritionFood beer = foodWithGroup("BEVERAGE");
        ReflectionTestUtils.setField(beer, "alcoholG", BigDecimal.valueOf(4.5));
        ReflectionTestUtils.setField(beer, "sodiumMg", BigDecimal.valueOf(600));

        DietProfile profile = new DietProfile(
                Set.of(DietRuleCode.ALCOHOL, DietRuleCode.SODIUM), true, defaultThresholds());

        DietAdvice advice = DietAdvisor.advise(beer, profile, Locale.ENGLISH);

        assertThat(advice).isNotNull();
        assertThat(advice.level()).isEqualTo(DietAdvisor.LIMIT);
        assertThat(advice.reasons()).extracting(DietAdvice.Reason::code)
                .containsExactly("ALCOHOL_LIMIT", "SODIUM_LIMIT");
    }

    @Test
    void skipsNaturalSugarGroupsFruitAndDairy() {
        NutritionFood mango = foodWithGroup("FRUIT");
        ReflectionTestUtils.setField(mango, "sugarsG", BigDecimal.valueOf(25));
        ReflectionTestUtils.setField(mango, "sodiumMg", BigDecimal.valueOf(5));

        DietProfile profile = new DietProfile(
                Set.of(DietRuleCode.SUGARS, DietRuleCode.SODIUM), true, defaultThresholds());

        DietAdvice advice = DietAdvisor.advise(mango, profile, Locale.ENGLISH);

        assertThat(advice).isNotNull();
        assertThat(advice.level()).isEqualTo(DietAdvisor.OK);
        assertThat(advice.reasons()).isEmpty();
    }

    @Test
    void returnsUnknownWhenRequiredSodiumIsMissing() {
        NutritionFood mystery = foodWithGroup("GRAIN");
        DietProfile profile = new DietProfile(Set.of(DietRuleCode.SODIUM), true, defaultThresholds());

        DietAdvice advice = DietAdvisor.advise(mystery, profile, Locale.forLanguageTag("vi"));

        assertThat(advice).isNotNull();
        assertThat(advice.level()).isEqualTo(DietAdvisor.UNKNOWN);
        assertThat(advice.reasons()).extracting(DietAdvice.Reason::code).containsExactly("SODIUM_UNKNOWN");
    }

    @Test
    void returnsGoodForHighMagnesiumLowSodiumAndGoodNaKRatio() {
        NutritionFood spinach = foodWithGroup("VEGETABLE");
        ReflectionTestUtils.setField(spinach, "sodiumMg", BigDecimal.valueOf(80));
        ReflectionTestUtils.setField(spinach, "potassiumMg", BigDecimal.valueOf(500));
        ReflectionTestUtils.setField(spinach, "magnesiumMg", BigDecimal.valueOf(79));

        DietProfile profile = new DietProfile(
                Set.of(DietRuleCode.SODIUM, DietRuleCode.NA_K_RATIO, DietRuleCode.MAGNESIUM),
                true,
                defaultThresholds());

        DietAdvice advice = DietAdvisor.advise(spinach, profile, Locale.ENGLISH);

        assertThat(advice).isNotNull();
        assertThat(advice.level()).isEqualTo(DietAdvisor.GOOD);
        assertThat(advice.reasons()).extracting(DietAdvice.Reason::code)
                .containsExactly("NA_K_RATIO_GOOD", "MAGNESIUM_GOOD");
    }
}
