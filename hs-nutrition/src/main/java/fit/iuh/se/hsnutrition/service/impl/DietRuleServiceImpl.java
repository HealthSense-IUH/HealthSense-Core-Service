package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.DietRuleResponse;
import fit.iuh.se.hsnutrition.dto.DietThresholdRequest;
import fit.iuh.se.hsnutrition.entity.NutritionDietRule;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.repository.NutritionDietRuleRepository;
import fit.iuh.se.hsnutrition.service.DietRuleService;
import fit.iuh.se.hsnutrition.service.DietThreshold;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DietRuleServiceImpl implements DietRuleService {
    private final NutritionDietRuleRepository rules;

    @Override
    public List<DietRuleResponse> list() {
        return rules.findAllByOrderByDisplayOrderAsc().stream().map(DietRuleServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public List<DietRuleResponse> update(List<DietThresholdRequest> thresholds) {
        if (thresholds == null || thresholds.isEmpty())
            throw new AppException(ErrorCode.INVALID_PARAMETER, "thresholds must not be empty");
        Set<DietRuleCode> seen = new HashSet<>();
        for (DietThresholdRequest request : thresholds) {
            DietRuleCode code = DietRuleService.parseCode(request.code());
            if (!seen.add(code)) throw new AppException(ErrorCode.INVALID_PARAMETER, "Duplicate diet rule: " + code);
            NutritionDietRule rule = rules.findById(code)
                    .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Diet rule not found: " + code));
            DietThreshold threshold = new DietThreshold(request.limit(), request.caution()).validated(rule.getName());
            if (threshold.limit() == null && threshold.caution() == null)
                throw new AppException(ErrorCode.INVALID_PARAMETER, rule.getName() + ": cần ít nhất một ngưỡng đỏ hoặc vàng");
            rule.setLimitThreshold(decimal(threshold.limit()));
            rule.setCautionThreshold(decimal(threshold.caution()));
        }
        rules.flush();
        return list();
    }

    @Override
    public Map<DietRuleCode, DietThreshold> defaults() {
        Map<DietRuleCode, DietThreshold> result = new EnumMap<>(DietRuleCode.class);
        for (NutritionDietRule rule : rules.findAll())
            result.put(rule.getCode(), new DietThreshold(NutrientMapper.amount(rule.getLimitThreshold()),
                    NutrientMapper.amount(rule.getCautionThreshold())));
        return result;
    }

    static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static DietRuleResponse toResponse(NutritionDietRule rule) {
        return new DietRuleResponse(rule.getCode().name(), rule.getName(), rule.getUnit(),
                NutrientMapper.amount(rule.getLimitThreshold()), NutrientMapper.amount(rule.getCautionThreshold()),
                rule.getUpdatedAt(), rule.getUpdatedBy());
    }
}
