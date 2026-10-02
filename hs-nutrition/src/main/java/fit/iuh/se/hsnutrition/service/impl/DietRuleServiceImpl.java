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
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-thresholds-empty");
        Set<DietRuleCode> seen = new HashSet<>();
        for (DietThresholdRequest request : thresholds) {
            DietRuleCode code = DietRuleService.parseCode(request.code());
            if (!seen.add(code)) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-rule-duplicate", code);
            NutritionDietRule rule = rules.findById(code)
                    .orElseThrow(() -> AppException.of(ErrorCode.ENTITY_NOT_FOUND, "detail.diet-rule-not-found", code));
            DietThreshold threshold = new DietThreshold(request.limit(), request.caution(), request.good())
                    .validated(rule.getName());
            if ((!code.usesGood() && threshold.good() != null)
                    || (!code.usesLimitOrCaution() && (threshold.limit() != null || threshold.caution() != null)))
                throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-unused", rule.getName());
            if (threshold.limit() == null && threshold.caution() == null && threshold.good() == null)
                throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-required", rule.getName());
            if (code == DietRuleCode.NA_K_RATIO && threshold.good() != null && threshold.limit() != null
                    && threshold.good() > threshold.limit())
                throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-good-order", rule.getName(),
                        threshold.good(), threshold.limit());
            rule.setLimitThreshold(decimal(threshold.limit()));
            rule.setCautionThreshold(decimal(threshold.caution()));
            rule.setGoodThreshold(decimal(threshold.good()));
        }
        rules.flush();
        return list();
    }

    @Override
    public Map<DietRuleCode, DietThreshold> defaults() {
        Map<DietRuleCode, DietThreshold> result = new EnumMap<>(DietRuleCode.class);
        for (NutritionDietRule rule : rules.findAll())
            result.put(rule.getCode(), new DietThreshold(NutrientMapper.amount(rule.getLimitThreshold()),
                    NutrientMapper.amount(rule.getCautionThreshold()), NutrientMapper.amount(rule.getGoodThreshold())));
        return result;
    }

    static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static DietRuleResponse toResponse(NutritionDietRule rule) {
        DietRuleCode code = rule.getCode();
        return new DietRuleResponse(code.name(), rule.getName(), rule.getUnit(),
                NutrientMapper.amount(rule.getLimitThreshold()), NutrientMapper.amount(rule.getCautionThreshold()),
                NutrientMapper.amount(rule.getGoodThreshold()), code.priority(), code.overridable(),
                rule.getEvidence(), rule.getEvidenceUrl(), rule.getUpdatedAt(), rule.getUpdatedBy());
    }
}
