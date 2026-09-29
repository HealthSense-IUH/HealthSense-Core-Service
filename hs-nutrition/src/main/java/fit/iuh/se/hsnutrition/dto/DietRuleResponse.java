package fit.iuh.se.hsnutrition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Ngưỡng mặc định của một quy tắc chấm màu (trang quản trị). {@code limit}: từ mức này là đỏ; {@code caution}: từ mức
 * này là vàng; trên 100 g, đơn vị {@code unit}. Không có mức nào thì trường đó vắng mặt.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DietRuleResponse(String code, String name, String unit, Double limit, Double caution, Instant updatedAt,
                               String updatedBy) {}
