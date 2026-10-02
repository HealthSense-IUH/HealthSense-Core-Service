package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Những gì cần để chấm màu thực phẩm cho một người: ngưỡng đã gộp (ngưỡng riêng của bác sĩ đè lên mặc định của admin)
 * và các quy tắc bác sĩ dặn riêng trong đơn. Bộ quy tắc nền cho người rung nhĩ ({@link DietRuleCode#base()}) luôn áp
 * dụng; quy tắc bác sĩ dặn thì lời nhắn ghi "Bác sĩ dặn bạn ...", riêng VITAMIN_K (đang dùng warfarin) bật thêm quy
 * tắc vitamin K. {@code personalized = false}: chưa có đơn.
 */
public record DietProfile(Set<DietRuleCode> prescribedRules, boolean personalized,
                          Map<DietRuleCode, DietThreshold> thresholds) {

    public DietProfile {
        prescribedRules = prescribedRules.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(prescribedRules));
        thresholds = Map.copyOf(thresholds);
    }

    /** Chưa có đơn: chỉ bộ quy tắc nền, theo ngưỡng mặc định. */
    public static DietProfile general(Map<DietRuleCode, DietThreshold> defaults) {
        return new DietProfile(Set.of(), false, defaults);
    }

    public boolean enabled(DietRuleCode code) {
        return code.base() || prescribed(code);
    }

    /** Bác sĩ có dặn riêng về quy tắc này trong đơn. */
    public boolean prescribed(DietRuleCode code) {
        return personalized && prescribedRules.contains(code);
    }

    public DietThreshold threshold(DietRuleCode code) {
        return thresholds.getOrDefault(code, new DietThreshold(null, null));
    }
}
