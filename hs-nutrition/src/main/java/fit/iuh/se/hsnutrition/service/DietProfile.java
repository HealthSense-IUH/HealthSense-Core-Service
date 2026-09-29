package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;

import java.util.Map;

/**
 * Những gì cần để chấm màu thực phẩm cho một người: quy tắc nào đang bật và ngưỡng đã gộp (ngưỡng riêng của bác sĩ
 * đè lên mặc định của admin). {@code personalized = false}: chưa có đơn, dùng lời khuyên chung.
 */
public record DietProfile(boolean limitSodium, boolean onWarfarin, boolean avoidAlcohol, boolean limitCaffeine,
                          boolean personalized, Map<DietRuleCode, DietThreshold> thresholds) {

    /** Lời khuyên chung khi chưa có đơn: hạn chế muối và tránh rượu bia, theo ngưỡng mặc định. */
    public static DietProfile general(Map<DietRuleCode, DietThreshold> defaults) {
        return new DietProfile(true, false, true, false, false, Map.copyOf(defaults));
    }

    public boolean enabled(DietRuleCode code) {
        return switch (code) {
            case SODIUM -> limitSodium;
            case ALCOHOL -> avoidAlcohol;
            case CAFFEINE -> limitCaffeine;
            case VITAMIN_K -> onWarfarin;
        };
    }

    public DietThreshold threshold(DietRuleCode code) {
        return thresholds.getOrDefault(code, new DietThreshold(null, null));
    }
}
