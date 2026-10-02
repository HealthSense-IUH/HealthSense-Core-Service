package fit.iuh.se.hsnutrition.dto;

/**
 * Ngưỡng của một quy tắc gửi lên: admin sửa mặc định, hoặc bác sĩ chỉnh riêng cho hội viên (chỉ các quy tắc
 * overridable, không có mức tốt). {@code code}: một DietRuleCode. Với bác sĩ, để trống một mức = dùng mặc định.
 */
public record DietThresholdRequest(String code, Double limit, Double caution, Double good) {
    public DietThresholdRequest(String code, Double limit, Double caution) {
        this(code, limit, caution, null);
    }
}
