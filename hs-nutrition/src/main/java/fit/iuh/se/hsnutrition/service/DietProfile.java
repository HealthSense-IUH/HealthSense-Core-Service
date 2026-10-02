package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;

import java.util.Map;

/**
 * Những gì cần để chấm màu thực phẩm cho một người: ngưỡng đã gộp (ngưỡng riêng của bác sĩ đè lên mặc định của admin)
 * và các cờ trong đơn. Bộ quy tắc nền cho người rung nhĩ ({@link DietRuleCode#base()}) luôn áp dụng; cờ của bác sĩ
 * chỉ đổi lời nhắn thành "Bác sĩ dặn bạn ...", riêng {@code onWarfarin} bật thêm quy tắc vitamin K.
 * {@code personalized = false}: chưa có đơn.
 */
public record DietProfile(boolean limitSodium, boolean onWarfarin, boolean avoidAlcohol, boolean limitCaffeine,
                          boolean personalized, Map<DietRuleCode, DietThreshold> thresholds) {

    /** Chưa có đơn: chỉ bộ quy tắc nền, theo ngưỡng mặc định. */
    public static DietProfile general(Map<DietRuleCode, DietThreshold> defaults) {
        return new DietProfile(false, false, false, false, false, Map.copyOf(defaults));
    }

    public boolean enabled(DietRuleCode code) {
        return code.base() || (code == DietRuleCode.VITAMIN_K && onWarfarin);
    }

    /** Bác sĩ có dặn riêng về quy tắc này trong đơn. */
    public boolean prescribed(DietRuleCode code) {
        return personalized && switch (code) {
            case SODIUM -> limitSodium;
            case ALCOHOL -> avoidAlcohol;
            case CAFFEINE -> limitCaffeine;
            case VITAMIN_K -> onWarfarin;
            default -> false;
        };
    }

    public DietThreshold threshold(DietRuleCode code) {
        return thresholds.getOrDefault(code, new DietThreshold(null, null));
    }
}
