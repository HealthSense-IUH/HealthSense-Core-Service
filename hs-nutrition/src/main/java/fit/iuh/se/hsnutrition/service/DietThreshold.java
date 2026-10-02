package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;

/**
 * Ngưỡng của một quy tắc, trên 100 g phần ăn được (tỷ lệ Na/K thì không có đơn vị), so "vượt quá mức này":
 * {@code limit} = đỏ (Nên hạn chế), {@code caution} = vàng (Cần lưu ý); {@code good} = mức tốt cho nhịp tim
 * (Na/K: từ mức này trở xuống; magie: từ mức này trở lên). null = quy tắc không có mức đó.
 */
public record DietThreshold(Double limit, Double caution, Double good) {
    public static final double MAX_VALUE = 1_000_000;

    public DietThreshold(Double limit, Double caution) {
        this(limit, caution, null);
    }

    /** Không âm, không quá lớn, và ngưỡng đỏ không thấp hơn ngưỡng vàng. */
    public DietThreshold validated(String label) {
        check(label, limit);
        check(label, caution);
        check(label, good);
        if (limit != null && caution != null && limit < caution)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-order", label, limit, caution);
        return this;
    }

    /** Ngưỡng riêng đè lên mặc định theo từng mức; mức nào để trống thì lấy mặc định. Mức tốt không chỉnh riêng. */
    public DietThreshold overriddenBy(Double overrideLimit, Double overrideCaution) {
        return new DietThreshold(overrideLimit != null ? overrideLimit : limit,
                overrideCaution != null ? overrideCaution : caution, good);
    }

    private static void check(String label, Double value) {
        if (value == null) return;
        if (value.isNaN() || value < 0 || value > MAX_VALUE)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-range", label, (long) MAX_VALUE);
    }
}
