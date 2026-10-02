package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsnutrition.dto.DietAdvice;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
import fit.iuh.se.hsnutrition.entity.NutritionFoodGroup;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.DietThreshold;
import fit.iuh.se.hsnutrition.service.impl.DietAdvisor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Bộ quy tắc rung nhĩ (V28) trên món giả, không cần cơ sở dữ liệu. */
class DietAdvisorTest {
    /** Ngưỡng khởi tạo của V28. */
    static final Map<DietRuleCode, DietThreshold> V28_DEFAULTS = Map.of(
            DietRuleCode.ALCOHOL, new DietThreshold(0.0, null),
            DietRuleCode.CAFFEINE, new DietThreshold(null, 80.0),
            DietRuleCode.SUGARS, new DietThreshold(10.0, 2.5),
            DietRuleCode.NA_K_RATIO, new DietThreshold(2.0, null, 1.0),
            DietRuleCode.SODIUM, new DietThreshold(400.0, 140.0),
            DietRuleCode.SATURATED_FAT, new DietThreshold(5.0, 1.5),
            DietRuleCode.MAGNESIUM, new DietThreshold(null, null, 50.0),
            DietRuleCode.VITAMIN_K, new DietThreshold(null, 100.0));
    static final DietProfile GENERAL = DietProfile.general(V28_DEFAULTS);

    /** Món giả: nhóm + các cặp (tên trường, giá trị). */
    private static NutritionFood food(String group, Object... fields) {
        NutritionFood food = new NutritionFood();
        if (group != null) {
            NutritionFoodGroup foodGroup = new NutritionFoodGroup();
            ReflectionTestUtils.setField(foodGroup, "id", group);
            ReflectionTestUtils.setField(food, "group", foodGroup);
        }
        for (int i = 0; i < fields.length; i += 2)
            ReflectionTestUtils.setField(food, (String) fields[i], new BigDecimal(fields[i + 1].toString()));
        return food;
    }

    private static List<String> codes(DietAdvice advice) {
        return advice.reasons().stream().map(DietAdvice.Reason::code).toList();
    }

    @Test
    void anyAlcoholIsRedAndThresholdsMeanStrictlyAbove() {
        DietAdvice beer = DietAdvisor.advise(food("BEVERAGE", "sodiumMg", 4, "alcoholG", 4.5, "caffeineMg", 0), GENERAL);
        assertEquals("LIMIT", beer.level());
        assertEquals(List.of("ALCOHOL_LIMIT"), codes(beer));
        assertTrue(beer.reasons().getFirst().message().contains("đỏ khi trên 0 g"), beer.toString());

        // Đúng bằng ngưỡng thì chưa vượt: 140 mg natri không vàng, 400 mg không đỏ
        assertEquals("OK", DietAdvisor.advise(food("CEREAL", "sodiumMg", 140), GENERAL).level());
        assertEquals("CAUTION", DietAdvisor.advise(food("CEREAL", "sodiumMg", 140.5), GENERAL).level());
        assertEquals("CAUTION", DietAdvisor.advise(food("CEREAL", "sodiumMg", 400), GENERAL).level());
        assertEquals("LIMIT", DietAdvisor.advise(food("CEREAL", "sodiumMg", 401), GENERAL).level());
    }

    @Test
    void caffeineSugarAndSaturatedFatFollowTheirBands() {
        DietAdvice espresso = DietAdvisor.advise(food("BEVERAGE", "sodiumMg", 14, "alcoholG", 0, "caffeineMg", 212), GENERAL);
        assertEquals(List.of("CAFFEINE_CAUTION"), codes(espresso));

        DietAdvice candy = DietAdvisor.advise(food("SWEET", "sodiumMg", 50, "sugarsG", 60, "fatSaturatedG", 1), GENERAL);
        assertEquals(List.of("SUGARS_LIMIT"), codes(candy));
        assertEquals(List.of("SUGARS_CAUTION"),
                codes(DietAdvisor.advise(food("CEREAL", "sodiumMg", 50, "sugarsG", 5), GENERAL)));

        // Đường tự nhiên: trái cây và sữa không bị chấm theo đường tổng
        assertEquals("OK", DietAdvisor.advise(food("FRUIT", "sodiumMg", 1, "sugarsG", 12), GENERAL).level());
        assertEquals("OK", DietAdvisor.advise(food("DAIRY", "sodiumMg", 44, "sugarsG", 5, "fatSaturatedG", 0.6), GENERAL).level());

        assertEquals(List.of("SATURATED_FAT_LIMIT"),
                codes(DietAdvisor.advise(food("FAT_OIL", "sodiumMg", 2, "fatSaturatedG", 51), GENERAL)));
        assertEquals(List.of("SATURATED_FAT_CAUTION"),
                codes(DietAdvisor.advise(food("EGG", "sodiumMg", 124, "fatSaturatedG", 3.1), GENERAL)));
    }

    @Test
    void sodiumPotassiumRatioIsRedOnlyWhenAlsoSaltyAndGoodWhenPotassiumWins() {
        // Na/K 4 và natri 800 mg -> đỏ, lý do Na/K (ưu tiên 2) đứng trước natri (ưu tiên 3)
        DietAdvice salty = DietAdvisor.advise(food("MIXED_DISH", "sodiumMg", 800, "potassiumMg", 200), GENERAL);
        assertEquals("LIMIT", salty.level());
        assertEquals(List.of("NA_K_RATIO_LIMIT", "SODIUM_LIMIT"), codes(salty));

        // Na/K 3 nhưng natri chỉ 300 mg -> không đỏ theo Na/K, chỉ vàng vì muối
        assertEquals(List.of("SODIUM_CAUTION"),
                codes(DietAdvisor.advise(food("MIXED_DISH", "sodiumMg", 300, "potassiumMg", 100), GENERAL)));

        // Kali nhiều hơn natri, không có điểm xấu -> xanh
        DietAdvice banana = DietAdvisor.advise(food("TUBER", "sodiumMg", 10, "potassiumMg", 400, "magnesiumMg", 20), GENERAL);
        assertEquals("GOOD", banana.level());
        assertEquals(List.of("NA_K_RATIO_GOOD"), codes(banana));

        // Na/K giữa 1 và 2, không gì khác -> không có lưu ý (OK), không tô xanh
        DietAdvice neutral = DietAdvisor.advise(food("CEREAL", "sodiumMg", 120, "potassiumMg", 80), GENERAL);
        assertEquals("OK", neutral.level());
        assertTrue(neutral.reasons().isEmpty());
    }

    @Test
    void magnesiumIsGoodOnlyWhenLowInSaltAndConcernsBeatStrengths() {
        DietAdvice almonds = DietAdvisor.advise(food("LEGUMES_NUTS", "sodiumMg", 1, "potassiumMg", 733,
                "magnesiumMg", 270, "fatSaturatedG", 1.2), GENERAL);
        assertEquals("GOOD", almonds.level());
        assertEquals(List.of("NA_K_RATIO_GOOD", "MAGNESIUM_GOOD"), codes(almonds));

        // Nhiều magie nhưng mặn -> không được điểm magie
        DietAdvice saltedNuts = DietAdvisor.advise(food("LEGUMES_NUTS", "sodiumMg", 300, "potassiumMg", 600,
                "magnesiumMg", 270), GENERAL);
        assertEquals(List.of("SODIUM_CAUTION"), codes(saltedNuts), "a concern hides the strengths");

        // Thiếu số liệu natri: không đoán, không cho điểm magie
        DietAdvice unknown = DietAdvisor.advise(food("LEGUMES_NUTS", "magnesiumMg", 270), GENERAL);
        assertEquals("UNKNOWN", unknown.level());
        assertEquals(List.of("SODIUM_UNKNOWN"), codes(unknown));
    }

    @Test
    void prescriptionAddsVitaminKAndPersonalizesTheMessages() {
        NutritionFood spinach = food("VEGETABLE", "sodiumMg", 79, "potassiumMg", 558, "magnesiumMg", 79, "vitaminKMcg", 483);
        assertEquals("GOOD", DietAdvisor.advise(spinach, GENERAL).level());

        DietProfile warfarin = new DietProfile(true, true, false, false, true, V28_DEFAULTS);
        DietAdvice onWarfarin = DietAdvisor.advise(spinach, warfarin);
        assertEquals(List.of("VITAMIN_K_CAUTION"), codes(onWarfarin));
        assertTrue(onWarfarin.personalized());

        NutritionFood soup = food("MIXED_DISH", "sodiumMg", 500, "potassiumMg", 400);
        assertTrue(DietAdvisor.advise(soup, warfarin).reasons().getFirst().message().contains("Bác sĩ dặn bạn hạn chế muối"));
        assertTrue(DietAdvisor.advise(soup, GENERAL).reasons().getFirst().message().contains("Người rung nhĩ nên hạn chế muối"));
    }
}
