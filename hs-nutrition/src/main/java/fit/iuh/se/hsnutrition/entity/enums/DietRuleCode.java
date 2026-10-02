package fit.iuh.se.hsnutrition.entity.enums;

/**
 * Các quy tắc chấm màu thực phẩm cho người rung nhĩ; khóa của bảng nutrition_diet_rules (V27, V28). Ngưỡng nằm trong
 * bảng (admin sửa được). Quy tắc chỉ có hiệu lực với hội viên khi bác sĩ tick quy tắc đó trong đơn ăn uống
 * (nutrition_diet_prescriptions, V26, V29); chưa có đơn hoặc đơn không tick ô nào thì món không được chấm màu.
 * <p>
 * {@code priority}: mức ưu tiên 1-4 (1 lọc cứng: cồn, caffeine, đường; 2 điện giải Na/K; 3 ngưỡng tim mạch chung:
 * natri, chất béo bão hòa; 4 vi chất bảo vệ: magie; 5 vitamin K cho người dùng warfarin). Lý do cùng mức màu xếp theo
 * thứ tự này, cũng là thứ tự khai báo.
 */
public enum DietRuleCode {
    ALCOHOL(1),
    CAFFEINE(1),
    SUGARS(1),
    NA_K_RATIO(2),
    SODIUM(3),
    SATURATED_FAT(3),
    MAGNESIUM(4),
    VITAMIN_K(5);

    private final int priority;

    DietRuleCode(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    /** Bác sĩ chỉnh ngưỡng riêng cho từng hội viên được (mọi quy tắc). */
    public boolean overridable() {
        return true;
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
