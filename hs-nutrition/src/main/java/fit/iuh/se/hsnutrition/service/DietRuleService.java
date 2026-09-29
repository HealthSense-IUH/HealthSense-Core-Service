package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.dto.DietRuleResponse;
import fit.iuh.se.hsnutrition.dto.DietThresholdRequest;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;

import java.util.List;
import java.util.Map;

/** Ngưỡng mặc định của các quy tắc chấm màu. Không kiểm tra quyền: controller chỉ cho admin sửa. */
public interface DietRuleService {
    List<DietRuleResponse> list();

    /** Sửa ngưỡng của các quy tắc có trong danh sách; mức để trống = bỏ mức đó. Trả về danh sách sau khi sửa. */
    List<DietRuleResponse> update(List<DietThresholdRequest> thresholds);

    Map<DietRuleCode, DietThreshold> defaults();

    static DietRuleCode parseCode(String code) {
        try {
            return DietRuleCode.valueOf(code == null ? "" : code.trim());
        } catch (IllegalArgumentException e) {
            throw new fit.iuh.se.hsshared.advice.entity.AppException(
                    fit.iuh.se.hsshared.advice.entity.enums.ErrorCode.INVALID_PARAMETER,
                    "Unknown diet rule: " + code + " (expected one of " + List.of(DietRuleCode.values()) + ")");
        }
    }
}
