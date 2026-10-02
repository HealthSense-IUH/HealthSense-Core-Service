package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Đơn ăn uống của một hội viên. {@code personalized = false}: bác sĩ chưa kê đơn, các cờ là lời khuyên chung
 * (hạn chế muối, tránh rượu bia) và không có thông tin người kê. {@code rules}: từng quy tắc với ngưỡng mặc định,
 * ngưỡng riêng (nếu bác sĩ chỉnh) và ngưỡng đang áp dụng.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NutritionDietPrescriptionResponse(
        Long memberId, boolean personalized, boolean limitSodium, boolean onWarfarin, boolean avoidAlcohol,
        boolean limitCaffeine, boolean limitSugars, boolean watchSodiumPotassium, boolean limitSaturatedFat,
        boolean encourageMagnesium, String note, Long prescribedBy, Long consultationSessionId, Instant updatedAt,
        List<Rule> rules) {

    /**
     * {@code enabled}: quy tắc đang áp dụng cho hội viên (bộ quy tắc nền luôn áp dụng; vitamin K khi dùng warfarin);
     * {@code prescribed}: bác sĩ dặn riêng; {@code overridable}: bác sĩ chỉnh ngưỡng riêng được;
     * {@code default*}: ngưỡng mặc định (admin); {@code limit}/{@code caution}/{@code good}: ngưỡng riêng của hội
     * viên (vắng = dùng mặc định); {@code effective*}: ngưỡng thực sự dùng để chấm màu.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Rule(String code, String name, String unit, boolean enabled, boolean prescribed, boolean overridable,
                       Double defaultLimit, Double defaultCaution, Double defaultGood, Double limit, Double caution,
                       Double good, Double effectiveLimit, Double effectiveCaution, Double effectiveGood) {}
}
