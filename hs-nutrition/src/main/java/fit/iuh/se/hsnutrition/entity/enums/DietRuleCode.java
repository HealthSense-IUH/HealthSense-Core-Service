package fit.iuh.se.hsnutrition.entity.enums;

/**
 * Các quy tắc chấm màu thực phẩm cho người rung nhĩ; khóa của bảng nutrition_diet_rules (V27, V28). Ngưỡng nằm trong
 * bảng (admin sửa được), ở đây chỉ là đặc điểm cố định của từng quy tắc:
 * <ul>
 *   <li>{@code priority}: mức ưu tiên 1-4 (1 lọc cứng: cồn, caffeine, đường; 2 điện giải Na/K; 3 ngưỡng tim mạch chung:
 *       natri, chất béo bão hòa; 4 vi chất bảo vệ: magie). Lý do cùng mức màu xếp theo thứ tự này.</li>
 *   <li>{@code base}: áp cho mọi người (bộ quy tắc nền). VITAMIN_K chỉ áp khi đơn ghi đang dùng warfarin.</li>
 *   <li>{@code overridable}: bác sĩ dặn riêng và chỉnh ngưỡng riêng cho từng hội viên được (cờ và cột trong
 *       nutrition_diet_prescriptions, V26, V27, V29). Hiện mọi quy tắc đều chỉnh riêng được.</li>
 * </ul>
 * Thứ tự khai báo là thứ tự hiển thị mặc định.
 */
public enum DietRuleCode {
    ALCOHOL(1, true, true),
    CAFFEINE(1, true, true),
    SUGARS(1, true, true),
    NA_K_RATIO(2, true, true),
    SODIUM(3, true, true),
    SATURATED_FAT(3, true, true),
    MAGNESIUM(4, true, true),
    VITAMIN_K(5, false, true);

    private final int priority;
    private final boolean base;
    private final boolean overridable;

    DietRuleCode(int priority, boolean base, boolean overridable) {
        this.priority = priority;
        this.base = base;
        this.overridable = overridable;
    }

    public int priority() {
        return priority;
    }

    public boolean base() {
        return base;
    }

    public boolean overridable() {
        return overridable;
    }

    /** Quy tắc có mức tốt cho nhịp tim (tỷ lệ Na/K, magie). */
    public boolean usesGood() {
        return this == NA_K_RATIO || this == MAGNESIUM;
    }

    /** Quy tắc có ngưỡng đỏ / vàng (mọi quy tắc trừ magie, vốn chỉ cộng điểm). */
    public boolean usesLimitOrCaution() {
        return this != MAGNESIUM;
    }
}
