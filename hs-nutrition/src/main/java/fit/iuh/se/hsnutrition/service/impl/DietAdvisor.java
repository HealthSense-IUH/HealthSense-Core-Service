package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.DietAdvice;
import fit.iuh.se.hsnutrition.dto.DietAdvice.Reason;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.DietThreshold;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Chấm màu một thực phẩm theo đơn ăn uống. Chỉ so số liệu trên 100 g với ngưỡng của từng quy tắc đang bật
 * ("từ mức này trở lên"): đạt ngưỡng đỏ là LIMIT, đạt ngưỡng vàng là CAUTION. Ngưỡng không cố định trong code:
 * admin đặt mặc định (nutrition_diet_rules), bác sĩ chỉnh riêng cho từng hội viên (xem DietProfile).
 * <p>
 * Thiếu số liệu cần cho một quy tắc thì không đoán: món không vi phạm quy tắc nào nhưng thiếu số liệu là UNKNOWN,
 * không phải OK. Cồn, caffeine chỉ bắt buộc có số liệu với nhóm đồ uống; vitamin K với rau và dầu mỡ; muối với mọi món.
 */
public final class DietAdvisor {
    public static final String OK = "OK";
    public static final String CAUTION = "CAUTION";
    public static final String LIMIT = "LIMIT";
    public static final String UNKNOWN = "UNKNOWN";

    static final Set<String> DRINK_GROUPS = Set.of("BEVERAGE");
    static final Set<String> VITAMIN_K_GROUPS = Set.of("VEGETABLE", "FAT_OIL");

    private static final List<String> SEVERITY = List.of(LIMIT, CAUTION, UNKNOWN);

    private DietAdvisor() {}

    public static DietAdvice advise(NutritionFood food, DietProfile profile) {
        String group = food.getGroup() == null ? null : food.getGroup().getId();
        List<Reason> reasons = new ArrayList<>();
        for (DietRuleCode code : DietRuleCode.values()) {
            if (!profile.enabled(code)) continue;
            Reason reason = check(code, value(food, code), group, profile.threshold(code), profile.personalized());
            if (reason != null) reasons.add(reason);
        }
        reasons.sort(Comparator.comparingInt(reason -> SEVERITY.indexOf(reason.level())));
        String level = reasons.isEmpty() ? OK : reasons.getFirst().level();
        return new DietAdvice(level, List.copyOf(reasons), profile.personalized());
    }

    private static Reason check(DietRuleCode code, Double value, String group, DietThreshold threshold,
                                boolean personalized) {
        if (value == null) {
            boolean required = switch (code) {
                case SODIUM -> true;
                case ALCOHOL, CAFFEINE -> DRINK_GROUPS.contains(group);
                case VITAMIN_K -> VITAMIN_K_GROUPS.contains(group);
            };
            return required ? new Reason(code + "_UNKNOWN", UNKNOWN, "Chưa có số liệu " + noun(code) + " của món này.") : null;
        }
        if (threshold.limit() != null && value >= threshold.limit())
            return new Reason(code + "_LIMIT", LIMIT, limitMessage(code, value, threshold.limit(), personalized));
        if (threshold.caution() != null && value >= threshold.caution())
            return new Reason(code + "_CAUTION", CAUTION, cautionMessage(code, value, threshold.caution(), personalized));
        return null;
    }

    private static String limitMessage(DietRuleCode code, double value, double limit, boolean personalized) {
        String amount = amount(code, value) + " (ngưỡng đỏ từ " + amount(code, limit) + ")";
        return switch (code) {
            case SODIUM -> "Nhiều muối: " + amount + ". " + advisedBy(personalized, "hạn chế muối") + ".";
            case ALCOHOL -> "Có cồn: " + amount + ". " + advisedBy(personalized, "tránh rượu bia")
                    + "; rượu bia dễ gây cơn rung nhĩ.";
            case CAFFEINE -> "Nhiều caffeine: " + amount + ". " + advisedBy(personalized, "hạn chế caffeine") + ".";
            case VITAMIN_K -> "Rất nhiều vitamin K: " + amount + ". Bạn đang dùng warfarin: hỏi bác sĩ trước khi ăn "
                    + "món này thường xuyên.";
        };
    }

    private static String cautionMessage(DietRuleCode code, double value, double caution, boolean personalized) {
        String amount = amount(code, value) + " (ngưỡng vàng từ " + amount(code, caution) + ")";
        return switch (code) {
            case SODIUM -> "Muối ở mức vừa: " + amount + ". Chú ý lượng ăn.";
            case ALCOHOL -> "Có cồn: " + amount + ". Chú ý lượng uống.";
            case CAFFEINE -> "Có caffeine: " + amount + ". " + advisedBy(personalized, "hạn chế caffeine") + ".";
            case VITAMIN_K -> "Nhiều vitamin K: " + amount + ". Bạn đang dùng warfarin: không cần kiêng, nhưng nên ăn "
                    + "lượng đều mỗi ngày, không tự ý ăn nhiều hơn hoặc bỏ hẳn.";
        };
    }

    private static String noun(DietRuleCode code) {
        return switch (code) {
            case SODIUM -> "muối (natri)";
            case ALCOHOL -> "cồn";
            case CAFFEINE -> "caffeine";
            case VITAMIN_K -> "vitamin K";
        };
    }

    private static String amount(DietRuleCode code, double value) {
        String unit = switch (code) {
            case SODIUM -> "mg natri";
            case ALCOHOL -> "g";
            case CAFFEINE -> "mg";
            case VITAMIN_K -> "µg";
        };
        return format(value) + " " + unit + "/100 g";
    }

    private static Double value(NutritionFood food, DietRuleCode code) {
        BigDecimal amount = switch (code) {
            case SODIUM -> food.getSodiumMg();
            case ALCOHOL -> food.getAlcoholG();
            case CAFFEINE -> food.getCaffeineMg();
            case VITAMIN_K -> food.getVitaminKMcg();
        };
        return amount == null ? null : amount.doubleValue();
    }

    private static String advisedBy(boolean personalized, String advice) {
        return personalized ? "Bác sĩ dặn bạn " + advice : "Người bệnh tim mạch nên " + advice;
    }

    private static String format(double amount) {
        // Dấu phẩy thập phân kiểu Việt: 482.9 -> "482,9", 600.0 -> "600"
        return NutrientMapper.amount(BigDecimal.valueOf(amount)).toString().replaceAll("\\.0$", "").replace('.', ',');
    }
}
