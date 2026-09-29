package fit.iuh.se.hsnutrition.dto;

/**
 * Ngưỡng của một quy tắc gửi lên: admin sửa mặc định, hoặc bác sĩ chỉnh riêng cho hội viên.
 * {@code code}: SODIUM | ALCOHOL | CAFFEINE | VITAMIN_K. Với bác sĩ, để trống một mức = dùng mặc định.
 */
public record DietThresholdRequest(String code, Double limit, Double caution) {}
