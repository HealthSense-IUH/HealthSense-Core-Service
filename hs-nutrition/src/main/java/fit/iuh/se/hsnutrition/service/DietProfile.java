package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Những gì cần để chấm màu thực phẩm cho một người: các quy tắc bác sĩ tick trong đơn và ngưỡng đã gộp (ngưỡng riêng
 * của bác sĩ đè lên mặc định của admin). Chỉ quy tắc được tick mới có hiệu lực; không có quy tắc nào (chưa có đơn,
 * {@code personalized = false}, hoặc đơn không tick ô nào) thì món không được chấm màu.
 */
public record DietProfile(Set<DietRuleCode> prescribedRules, boolean personalized,
                          Map<DietRuleCode, DietThreshold> thresholds) {

    public DietProfile {
        prescribedRules = prescribedRules.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(prescribedRules));
        thresholds = Map.copyOf(thresholds);
    }

    /** Chưa có đơn: không quy tắc nào, món không được chấm màu. */
    public static DietProfile general(Map<DietRuleCode, DietThreshold> defaults) {
        return new DietProfile(Set.of(), false, defaults);
    }

    /** Quy tắc có hiệu lực: bác sĩ tick quy tắc này trong đơn. */
    public boolean enabled(DietRuleCode code) {
        return personalized && prescribedRules.contains(code);
    }

    /** Có ít nhất một quy tắc để chấm màu. */
    public boolean rates() {
        return personalized && !prescribedRules.isEmpty();
    }

    public DietThreshold threshold(DietRuleCode code) {
        return thresholds.getOrDefault(code, new DietThreshold(null, null));
    }
}
