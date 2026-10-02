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

    /** null khi đơn không có quy tắc nào để xét. */
    public static DietAdvice advise(NutritionFood food, DietProfile profile) {
        if (!profile.rates()) return null;
        String group = food.getGroup() == null ? null : food.getGroup().getId();
        List<Reason> concerns = new ArrayList<>();
        List<Reason> strengths = new ArrayList<>();
        for (DietRuleCode code : DietRuleCode.values()) {
            if (!profile.enabled(code)) continue;
            Reason reason = switch (code) {
                case NA_K_RATIO -> sodiumPotassium(food, profile);
                case MAGNESIUM -> magnesium(food, profile);
                default -> threshold(code, food, group, profile);
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
    private static Reason threshold(DietRuleCode code, NutritionFood food, String group, DietProfile profile) {
        if (code == DietRuleCode.SUGARS && NATURAL_SUGAR_GROUPS.contains(group)) return null;
        Double value = value(food, code);
        if (value == null) {
            boolean required = switch (code) {
                case SODIUM -> true;
                case ALCOHOL, CAFFEINE -> DRINK_GROUPS.contains(group);
                case VITAMIN_K -> VITAMIN_K_GROUPS.contains(group);
                default -> false;
            };
            return required ? new Reason(code + "_UNKNOWN", UNKNOWN, "Chưa có số liệu " + noun(code) + " của món này.") : null;
        }
        DietThreshold threshold = profile.threshold(code);
        boolean prescribed = profile.enabled(code);
        if (threshold.limit() != null && value > threshold.limit())
            return new Reason(code + "_LIMIT", LIMIT, limitMessage(code, value, threshold.limit(), prescribed));
        if (threshold.caution() != null && value > threshold.caution())
            return new Reason(code + "_CAUTION", CAUTION, cautionMessage(code, value, threshold.caution(), prescribed));
        return null;
    }

    /** Tỷ lệ natri/kali (mg/mg): đỏ khi cao và món cũng nhiều muối; tốt khi kali bằng hoặc hơn natri. */
    private static Reason sodiumPotassium(NutritionFood food, DietProfile profile) {
        Double sodium = value(food, DietRuleCode.SODIUM);
        Double potassium = amount(food.getPotassiumMg());
        if (sodium == null || potassium == null || potassium <= 0) return null;
        double ratio = sodium / potassium;
        DietThreshold threshold = profile.threshold(DietRuleCode.NA_K_RATIO);
        Double saltyFrom = profile.threshold(DietRuleCode.SODIUM).limit();
        if (threshold.limit() != null && ratio > threshold.limit() && (saltyFrom == null || sodium > saltyFrom))
            return new Reason("NA_K_RATIO_LIMIT", LIMIT, "Natri cao hơn nhiều so với kali (Na/K " + format(ratio)
                    + ", đỏ khi trên " + format(threshold.limit()) + (saltyFrom == null ? "" : " và natri trên "
                    + format(saltyFrom) + " mg") + "): dễ gây giữ nước, tăng áp lực lên tim và khởi phát rung nhĩ."
                    + (profile.enabled(DietRuleCode.NA_K_RATIO) ? " Bác sĩ dặn bạn theo dõi tỷ lệ natri/kali." : ""));
        if (threshold.good() != null && ratio <= threshold.good())
            return new Reason("NA_K_RATIO_GOOD", GOOD, "Kali bằng hoặc nhiều hơn natri (Na/K " + format(ratio)
                    + "): giúp ổn định nhịp tim.");
        return null;
    }

    /** Magie: tốt khi giàu magie mà ít muối. */
    private static Reason magnesium(NutritionFood food, DietProfile profile) {
        Double magnesium = amount(food.getMagnesiumMg());
        Double sodium = value(food, DietRuleCode.SODIUM);
        Double goodFrom = profile.threshold(DietRuleCode.MAGNESIUM).good();
        Double lowSaltUpTo = profile.threshold(DietRuleCode.SODIUM).caution();
        if (magnesium == null || goodFrom == null || magnesium < goodFrom) return null;
        if (lowSaltUpTo != null && (sodium == null || sodium > lowSaltUpTo)) return null;
        return new Reason("MAGNESIUM_GOOD", GOOD, "Giàu magie (" + format(magnesium) + " mg/100 g) và ít muối: "
                + "hỗ trợ ổn định điện thế cơ tim." + (profile.enabled(DietRuleCode.MAGNESIUM)
                ? " Bác sĩ khuyến khích bạn ăn món giàu magie." : ""));
    }

    private static String limitMessage(DietRuleCode code, double value, double limit, boolean prescribed) {
        String amount = amount(code, value) + " (đỏ khi trên " + amount(code, limit) + ")";
        return switch (code) {
            case ALCOHOL -> "Có cồn: " + amount + ". " + advisedBy(prescribed, "tránh rượu bia")
                    + "; cồn là yếu tố kích phát cơn rung nhĩ rõ nhất.";
            case CAFFEINE -> "Rất nhiều caffeine: " + amount + ". " + advisedBy(prescribed, "hạn chế caffeine") + ".";
            case SUGARS -> "Nhiều đường: " + amount + ". " + advisedBy(prescribed, "hạn chế đồ ngọt") + ".";
            case SODIUM -> "Nhiều muối: " + amount + ". " + advisedBy(prescribed, "hạn chế muối") + ".";
            case SATURATED_FAT -> "Nhiều chất béo bão hòa: " + amount + ". "
                    + advisedBy(prescribed, "hạn chế chất béo bão hòa") + ".";
            case VITAMIN_K -> "Rất nhiều vitamin K: " + amount + ". Bạn đang dùng warfarin: hỏi bác sĩ trước khi ăn "
                    + "món này thường xuyên.";
            default -> throw new IllegalStateException("No threshold message for " + code);
        };
    }

    private static String cautionMessage(DietRuleCode code, double value, double caution, boolean prescribed) {
        String amount = amount(code, value) + " (vàng khi trên " + amount(code, caution) + ")";
        return switch (code) {
            case ALCOHOL -> "Có cồn: " + amount + ". Chú ý lượng uống.";
            case CAFFEINE -> "Nhiều caffeine: " + amount + ". " + advisedBy(prescribed, "dùng caffeine vừa phải")
                    + "; liều cao làm tim đập nhanh.";
            case SUGARS -> "Đường ở mức vừa: " + amount + ". Chú ý lượng ăn.";
            case SODIUM -> "Muối ở mức vừa: " + amount + ". Chú ý lượng ăn.";
            case SATURATED_FAT -> "Chất béo bão hòa ở mức vừa: " + amount + ". Chú ý lượng ăn.";
            case VITAMIN_K -> "Nhiều vitamin K: " + amount + ". Bạn đang dùng warfarin: không cần kiêng, nhưng nên ăn "
                    + "lượng đều mỗi ngày, không tự ý ăn nhiều hơn hoặc bỏ hẳn.";
            default -> throw new IllegalStateException("No threshold message for " + code);
        };
    }

    private static String noun(DietRuleCode code) {
        return switch (code) {
            case SODIUM -> "muối (natri)";
            case ALCOHOL -> "cồn";
            case CAFFEINE -> "caffeine";
            case VITAMIN_K -> "vitamin K";
            default -> code.name();
        };
    }

    private static String amount(DietRuleCode code, double value) {
        String unit = switch (code) {
            case SODIUM -> "mg natri";
            case ALCOHOL, SUGARS, SATURATED_FAT -> "g";
            case VITAMIN_K -> "µg";
            default -> "mg";
        };
        return format(value) + " " + unit + "/100 g";
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

    private static String advisedBy(boolean prescribed, String advice) {
        return prescribed ? "Bác sĩ dặn bạn " + advice : "Nên " + advice;
    }

    private static String format(double amount) {
        // Dấu phẩy thập phân kiểu Việt, tối đa 2 chữ số: 482.9 -> "482,9", 600.0 -> "600", 0.4166 -> "0,42"
        return NutrientMapper.amount(BigDecimal.valueOf(Math.round(amount * 100) / 100.0)).toString()
                .replaceAll("\\.0$", "").replace('.', ',');
    }
}
