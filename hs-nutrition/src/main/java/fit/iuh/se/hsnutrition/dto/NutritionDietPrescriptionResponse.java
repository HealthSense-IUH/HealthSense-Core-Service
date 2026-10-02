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
        boolean limitCaffeine, String note, Long prescribedBy, Long consultationSessionId, Instant updatedAt,
        List<Rule> rules) {

    /**
     * {@code enabled}: quy tắc đang áp dụng cho hội viên (bộ quy tắc nền luôn áp dụng; vitamin K khi dùng warfarin);
     * {@code prescribed}: bác sĩ dặn riêng; {@code overridable}: bác sĩ chỉnh ngưỡng riêng được;
     * {@code limit}/{@code caution}: ngưỡng riêng của hội viên (vắng = dùng mặc định);
     * {@code effectiveLimit}/{@code effectiveCaution}: ngưỡng thực sự dùng để chấm màu; {@code good}: mức tốt.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Rule(String code, String name, String unit, boolean enabled, boolean prescribed, boolean overridable,
                       Double defaultLimit, Double defaultCaution, Double limit, Double caution, Double effectiveLimit,
                       Double effectiveCaution, Double good) {}
}
