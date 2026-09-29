package fit.iuh.se.hsnutrition.dto;

import java.util.List;

/**
 * Đánh giá một thực phẩm theo đơn ăn uống của người đang xem.
 * {@code level}: OK (xanh), CAUTION (vàng), LIMIT (đỏ), UNKNOWN (xám - thiếu số liệu để đánh giá).
 * {@code reasons}: lý do theo thứ tự nặng trước; OK thì rỗng. {@code personalized}: theo đơn của bác sĩ
 * hay theo lời khuyên chung.
 */
public record DietAdvice(String level, List<Reason> reasons, boolean personalized) {
    public record Reason(String code, String level, String message) {}
}
