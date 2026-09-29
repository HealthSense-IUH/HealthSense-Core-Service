package fit.iuh.se.hsnutrition.dto;

import java.util.List;

/**
 * Bác sĩ kê hoặc sửa đơn ăn uống; ghi đè toàn bộ đơn hiện có. {@code note} tối đa 1000 ký tự.
 * {@code thresholds}: ngưỡng riêng cho hội viên này; quy tắc không có trong danh sách, hoặc mức để trống, dùng mặc định.
 */
public record UpdateDietPrescriptionRequest(
        boolean limitSodium, boolean onWarfarin, boolean avoidAlcohol, boolean limitCaffeine, String note,
        List<DietThresholdRequest> thresholds) {}
