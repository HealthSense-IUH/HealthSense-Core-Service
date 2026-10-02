package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Ngưỡng mặc định của một quy tắc chấm màu (trang quản trị). {@code limit}: vượt quá là đỏ; {@code caution}: vượt quá
 * là vàng; {@code good}: mức tốt cho nhịp tim; trên 100 g, đơn vị {@code unit} (tỷ lệ Na/K không có đơn vị). Không có
 * mức nào thì trường đó vắng mặt. {@code priority}: mức ưu tiên 1-5; {@code overridable}: bác sĩ chỉnh riêng được;
 * {@code evidence}/{@code evidenceUrl}: nguồn của quy tắc. Quy tắc chỉ có hiệu lực khi bác sĩ tick trong đơn.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DietRuleResponse(String code, String name, String unit, Double limit, Double caution, Double good,
                               int priority, boolean overridable, String evidence, String evidenceUrl,
                               Instant updatedAt, String updatedBy) {}
