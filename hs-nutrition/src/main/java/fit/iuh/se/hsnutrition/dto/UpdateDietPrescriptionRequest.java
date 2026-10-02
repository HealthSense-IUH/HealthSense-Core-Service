package fit.iuh.se.hsnutrition.dto;

import java.util.List;

/**
 * Bác sĩ kê hoặc sửa đơn ăn uống; ghi đè toàn bộ đơn hiện có. Mỗi cờ ứng với một quy tắc (DietRuleCode): bác sĩ dặn
 * riêng quy tắc đó; {@code onWarfarin} bật thêm quy tắc vitamin K. 4 cờ thêm ở V29 có thể vắng (Frontend cũ) và
 * khi đó tính là false. {@code note} tối đa 1000 ký tự.
 * {@code thresholds}: ngưỡng riêng cho hội viên này; quy tắc không có trong danh sách, hoặc mức để trống, dùng mặc định.
 */
public record UpdateDietPrescriptionRequest(
        boolean limitSodium, boolean onWarfarin, boolean avoidAlcohol, boolean limitCaffeine, Boolean limitSugars,
        Boolean watchSodiumPotassium, Boolean limitSaturatedFat, Boolean encourageMagnesium, String note,
        List<DietThresholdRequest> thresholds) {

    public UpdateDietPrescriptionRequest {
        limitSugars = Boolean.TRUE.equals(limitSugars);
        watchSodiumPotassium = Boolean.TRUE.equals(watchSodiumPotassium);
        limitSaturatedFat = Boolean.TRUE.equals(limitSaturatedFat);
        encourageMagnesium = Boolean.TRUE.equals(encourageMagnesium);
    }

    /** Đơn chỉ có 4 cờ ban đầu (muối, warfarin, cồn, caffeine). */
    public UpdateDietPrescriptionRequest(boolean limitSodium, boolean onWarfarin, boolean avoidAlcohol,
                                         boolean limitCaffeine, String note, List<DietThresholdRequest> thresholds) {
        this(limitSodium, onWarfarin, avoidAlcohol, limitCaffeine, false, false, false, false, note, thresholds);
    }
}
