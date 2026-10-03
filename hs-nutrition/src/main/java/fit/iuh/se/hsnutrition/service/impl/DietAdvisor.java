package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.DietAdvice;
import fit.iuh.se.hsnutrition.dto.DietAdvice.Reason;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.DietThreshold;
import fit.iuh.se.hsshared.i18n.RequestLanguage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Chấm màu một thực phẩm cho người rung nhĩ theo đơn ăn uống, trên số liệu 100 g phần ăn được. Chỉ các quy tắc bác sĩ
 * tick trong đơn mới được xét; đơn không có quy tắc nào (hoặc chưa có đơn) thì không chấm màu (trả về null). Ngưỡng
 * không cố định trong code: admin đặt mặc định (nutrition_diet_rules), bác sĩ chỉnh riêng cho từng hội viên.
 * <p>
 * Thứ tự ưu tiên (V28):
 * <ol>
 *   <li>Lọc cứng: cồn trên ngưỡng đỏ (mặc định 0 g, tức có cồn) là đỏ; caffeine, đường vượt ngưỡng vàng là vàng.
 *       Đường tính trên đường tổng (USDA không có đường bổ sung) nên bỏ qua trái cây và sữa (đường tự nhiên).</li>
 *   <li>Tỷ lệ natri/kali: đỏ khi vượt ngưỡng đỏ và natri cũng vượt ngưỡng đỏ của muối; tốt khi không vượt mức tốt.</li>
 *   <li>Natri, chất béo bão hòa: vượt ngưỡng vàng là vàng, vượt ngưỡng đỏ là đỏ.</li>
 *   <li>Magie: tốt khi đạt mức tốt và natri không vượt ngưỡng vàng của muối.</li>
 * </ol>
 * Gộp màu: có quy tắc đỏ là đỏ (LIMIT); không có đỏ mà có vàng là vàng (CAUTION); thiếu số liệu bắt buộc là xám
 * (UNKNOWN, không đoán); không có điểm xấu và đạt ít nhất một điểm tốt là xanh (GOOD); còn lại là OK (không có lưu ý).
 * Lý do xếp theo mức màu rồi theo thứ tự ưu tiên. Muối bắt buộc có số liệu với mọi món; cồn, caffeine với đồ uống;
 * vitamin K với rau và dầu mỡ. Quy tắc khác thiếu số liệu thì bỏ qua.
 * <p>
 * Câu giải thích theo ngôn ngữ của request (header {@code lang}), xem {@link DietAdviceMessages}.
 */
public final class DietAdvisor {
    public static final String GOOD = "GOOD";
    public static final String OK = "OK";
    public static final String CAUTION = "CAUTION";
    public static final String LIMIT = "LIMIT";
    public static final String UNKNOWN = "UNKNOWN";

    static final Set<String> DRINK_GROUPS = Set.of("BEVERAGE");
    static final Set<String> VITAMIN_K_GROUPS = Set.of("VEGETABLE", "FAT_OIL");
    /** Đường tự nhiên: đường tổng không phản ánh đường bổ sung nên không chấm đường cho các nhóm này. */
    static final Set<String> NATURAL_SUGAR_GROUPS = Set.of("FRUIT", "DAIRY");

    private static final List<String> SEVERITY = List.of(LIMIT, CAUTION, UNKNOWN);

    private DietAdvisor() {}

    /** null khi đơn không có quy tắc nào để xét. Câu giải thích theo ngôn ngữ của request đang xử lý. */
    public static DietAdvice advise(NutritionFood food, DietProfile profile) {
        return advise(food, profile, RequestLanguage.current());
    }

    public static DietAdvice advise(NutritionFood food, DietProfile profile, Locale locale) {
        if (!profile.rates()) return null;
        DietAdviceMessages messages = new DietAdviceMessages(locale);
        String group = food.getGroup() == null ? null : food.getGroup().getId();
        List<Reason> concerns = new ArrayList<>();
        List<Reason> strengths = new ArrayList<>();
        for (DietRuleCode code : DietRuleCode.values()) {
            if (!profile.enabled(code)) continue;
            Reason reason = switch (code) {
                case NA_K_RATIO -> sodiumPotassium(food, profile, messages);
                case MAGNESIUM -> magnesium(food, profile, messages);
                default -> threshold(code, food, group, profile, messages);
            };
            if (reason == null) continue;
            (GOOD.equals(reason.level()) ? strengths : concerns).add(reason);
        }
        // Ổn định: cùng mức màu thì giữ thứ tự ưu tiên của DietRuleCode
        concerns.sort(Comparator.comparingInt(reason -> SEVERITY.indexOf(reason.level())));
        if (!concerns.isEmpty())
            return new DietAdvice(concerns.getFirst().level(), List.copyOf(concerns), profile.personalized());
        if (!strengths.isEmpty()) return new DietAdvice(GOOD, List.copyOf(strengths), profile.personalized());
        return new DietAdvice(OK, List.of(), profile.personalized());
    }

    /** Quy tắc so một chất với ngưỡng đỏ / vàng (cồn, caffeine, đường, natri, chất béo bão hòa, vitamin K). */
    private static Reason threshold(DietRuleCode code, NutritionFood food, String group, DietProfile profile,
                                    DietAdviceMessages messages) {
        if (code == DietRuleCode.SUGARS && NATURAL_SUGAR_GROUPS.contains(group)) return null;
        Double value = value(food, code);
        if (value == null) {
            boolean required = switch (code) {
                case SODIUM -> true;
                case ALCOHOL, CAFFEINE -> DRINK_GROUPS.contains(group);
                case VITAMIN_K -> VITAMIN_K_GROUPS.contains(group);
                default -> false;
            };
            return required
                    ? new Reason(code + "_UNKNOWN", UNKNOWN, messages.get("unknown", messages.get("noun." + code)))
                    : null;
        }
        DietThreshold threshold = profile.threshold(code);
        boolean prescribed = profile.enabled(code);
        if (threshold.limit() != null && value > threshold.limit())
            return new Reason(code + "_LIMIT", LIMIT, limitMessage(code, value, threshold.limit(), prescribed, messages));
        if (threshold.caution() != null && value > threshold.caution())
            return new Reason(code + "_CAUTION", CAUTION, cautionMessage(code, value, threshold.caution(), prescribed, messages));
        return null;
    }

    /** Tỷ lệ natri/kali (mg/mg): đỏ khi cao và món cũng nhiều muối; tốt khi kali bằng hoặc hơn natri. */
    private static Reason sodiumPotassium(NutritionFood food, DietProfile profile, DietAdviceMessages messages) {
        Double sodium = value(food, DietRuleCode.SODIUM);
        Double potassium = amount(food.getPotassiumMg());
        if (sodium == null || potassium == null || potassium <= 0) return null;
        double ratio = sodium / potassium;
        DietThreshold threshold = profile.threshold(DietRuleCode.NA_K_RATIO);
        Double saltyFrom = profile.threshold(DietRuleCode.SODIUM).limit();
        if (threshold.limit() != null && ratio > threshold.limit() && (saltyFrom == null || sodium > saltyFrom)) {
            String salty = saltyFrom == null ? "" : messages.get("naK.limit.salty", messages.number(saltyFrom));
            String message = messages.get("naK.limit", messages.number(ratio), messages.number(threshold.limit()), salty);
            if (profile.enabled(DietRuleCode.NA_K_RATIO)) message += " " + messages.get("naK.limit.prescribed");
            return new Reason("NA_K_RATIO_LIMIT", LIMIT, message);
        }
        if (threshold.good() != null && ratio <= threshold.good())
            return new Reason("NA_K_RATIO_GOOD", GOOD, messages.get("naK.good", messages.number(ratio)));
        return null;
    }

    /** Magie: tốt khi giàu magie mà ít muối. */
    private static Reason magnesium(NutritionFood food, DietProfile profile, DietAdviceMessages messages) {
        Double magnesium = amount(food.getMagnesiumMg());
        Double sodium = value(food, DietRuleCode.SODIUM);
        Double goodFrom = profile.threshold(DietRuleCode.MAGNESIUM).good();
        Double lowSaltUpTo = profile.threshold(DietRuleCode.SODIUM).caution();
        if (magnesium == null || goodFrom == null || magnesium < goodFrom) return null;
        if (lowSaltUpTo != null && (sodium == null || sodium > lowSaltUpTo)) return null;
        String message = messages.get("magnesium.good", messages.number(magnesium));
        if (profile.enabled(DietRuleCode.MAGNESIUM)) message += " " + messages.get("magnesium.good.prescribed");
        return new Reason("MAGNESIUM_GOOD", GOOD, message);
    }

    private static String limitMessage(DietRuleCode code, double value, double limit, boolean prescribed,
                                       DietAdviceMessages messages) {
        if (code == DietRuleCode.NA_K_RATIO || code == DietRuleCode.MAGNESIUM)
            throw new IllegalStateException("No threshold message for " + code);
        String amount = messages.get("limitAmount", amount(code, value, messages), amount(code, limit, messages));
        String key = "limit." + code;
        return code == DietRuleCode.VITAMIN_K
                ? messages.get(key, amount)
                : messages.get(key, amount, advisedBy(prescribed, messages.get(key + ".advice"), messages));
    }

    private static String cautionMessage(DietRuleCode code, double value, double caution, boolean prescribed,
                                         DietAdviceMessages messages) {
        if (code == DietRuleCode.NA_K_RATIO || code == DietRuleCode.MAGNESIUM)
            throw new IllegalStateException("No threshold message for " + code);
        String amount = messages.get("cautionAmount", amount(code, value, messages), amount(code, caution, messages));
        String key = "caution." + code;
        return code == DietRuleCode.CAFFEINE
                ? messages.get(key, amount, advisedBy(prescribed, messages.get(key + ".advice"), messages))
                : messages.get(key, amount);
    }

    private static String amount(DietRuleCode code, double value, DietAdviceMessages messages) {
        String unit = switch (code) {
            case SODIUM -> messages.get("unit.SODIUM");
            case ALCOHOL, SUGARS, SATURATED_FAT -> "g";
            case VITAMIN_K -> "µg";
            default -> "mg";
        };
        return messages.get("amount", messages.number(value), unit);
    }

    private static Double value(NutritionFood food, DietRuleCode code) {
        return amount(switch (code) {
            case SODIUM -> food.getSodiumMg();
            case ALCOHOL -> food.getAlcoholG();
            case CAFFEINE -> food.getCaffeineMg();
            case SUGARS -> food.getSugarsG();
            case SATURATED_FAT -> food.getFatSaturatedG();
            case VITAMIN_K -> food.getVitaminKMcg();
            case NA_K_RATIO, MAGNESIUM -> null;
        });
    }

    private static Double amount(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private static String advisedBy(boolean prescribed, String advice, DietAdviceMessages messages) {
        return messages.get(prescribed ? "advised.prescribed" : "advised.general", advice);
    }
}
