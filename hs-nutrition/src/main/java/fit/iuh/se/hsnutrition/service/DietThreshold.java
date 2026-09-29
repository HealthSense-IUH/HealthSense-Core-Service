package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;

/**
 * Ngưỡng của một quy tắc, trên 100 g phần ăn được, so "từ mức này trở lên":
 * {@code limit} = đỏ (Nên hạn chế), {@code caution} = vàng (Cần lưu ý); null = không có mức đó.
 */
public record DietThreshold(Double limit, Double caution) {
    public static final double MAX_VALUE = 1_000_000;

    /** Không âm, không quá lớn, và ngưỡng đỏ không thấp hơn ngưỡng vàng. */
    public DietThreshold validated(String label) {
        check(label, limit);
        check(label, caution);
        if (limit != null && caution != null && limit < caution)
            throw new AppException(ErrorCode.INVALID_PARAMETER,
                    label + ": ngưỡng đỏ (" + limit + ") phải lớn hơn hoặc bằng ngưỡng vàng (" + caution + ")");
        return this;
    }

    /** Ngưỡng riêng đè lên mặc định theo từng mức; mức nào để trống thì lấy mặc định. */
    public DietThreshold overriddenBy(Double overrideLimit, Double overrideCaution) {
        return new DietThreshold(overrideLimit != null ? overrideLimit : limit,
                overrideCaution != null ? overrideCaution : caution);
    }

    private static void check(String label, Double value) {
        if (value == null) return;
        if (value.isNaN() || value < 0 || value > MAX_VALUE)
            throw new AppException(ErrorCode.INVALID_PARAMETER, label + ": ngưỡng phải từ 0 đến " + (long) MAX_VALUE);
    }
}
