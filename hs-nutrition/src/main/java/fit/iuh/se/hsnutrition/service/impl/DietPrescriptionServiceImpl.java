package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.DietRuleResponse;
import fit.iuh.se.hsnutrition.dto.DietThresholdRequest;
import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse;
import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse.Rule;
import fit.iuh.se.hsnutrition.dto.UpdateDietPrescriptionRequest;
import fit.iuh.se.hsnutrition.entity.NutritionDietPrescription;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.repository.NutritionDietPrescriptionRepository;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsnutrition.service.DietProfile;
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
public class DietPrescriptionServiceImpl implements DietPrescriptionService {
    static final int MAX_NOTE_LENGTH = 1000;

    private final NutritionDietPrescriptionRepository prescriptions;
    private final DietRuleService rules;

    @Override
    public NutritionDietPrescriptionResponse get(Long memberId) {
        List<DietRuleResponse> definitions = rules.list();
        return prescriptions.findById(memberId).map(p -> toResponse(p, definitions))
                .orElseGet(() -> general(memberId, definitions));
    }

    @Override
    @Transactional
    public NutritionDietPrescriptionResponse save(Long memberId, Long doctorId, Long consultationSessionId,
                                                  UpdateDietPrescriptionRequest request) {
        NutritionDietPrescription prescription = prescriptions.findById(memberId).orElseGet(() -> {
            NutritionDietPrescription created = new NutritionDietPrescription();
            created.setMemberId(memberId);
            return created;
        });
        prescription.setLimitSodium(request.limitSodium());
        prescription.setOnWarfarin(request.onWarfarin());
        prescription.setAvoidAlcohol(request.avoidAlcohol());
        prescription.setLimitCaffeine(request.limitCaffeine());
        prescription.setNote(normalizeNote(request.note()));
        applyOverrides(prescription, request.thresholds());
        prescription.setPrescribedBy(doctorId);
        prescription.setConsultationSessionId(consultationSessionId);
        return toResponse(prescriptions.saveAndFlush(prescription), rules.list());
    }

    @Override
    public DietProfile profileOf(Long memberId) {
        Map<DietRuleCode, DietThreshold> defaults = rules.defaults();
        return prescriptions.findById(memberId)
                .map(p -> new DietProfile(p.isLimitSodium(), p.isOnWarfarin(), p.isAvoidAlcohol(), p.isLimitCaffeine(),
                        true, effective(p, defaults)))
                .orElseGet(() -> DietProfile.general(defaults));
    }

    /**
     * Ghi đè toàn bộ ngưỡng riêng: quy tắc không gửi lên thì về mặc định. Ngưỡng sau khi gộp với mặc định phải hợp
     * lệ (đỏ không thấp hơn vàng), để bác sĩ không vô tình tạo cặp ngưỡng ngược nhau.
     */
    private void applyOverrides(NutritionDietPrescription prescription, List<DietThresholdRequest> thresholds) {
        Map<DietRuleCode, DietThreshold> defaults = rules.defaults();
        for (DietRuleCode code : DietRuleCode.values()) prescription.setOverride(code, null, null);
        if (thresholds == null) return;
        Set<DietRuleCode> seen = new HashSet<>();
        for (DietThresholdRequest request : thresholds) {
            DietRuleCode code = DietRuleService.parseCode(request.code());
            if (!seen.add(code)) throw new AppException(ErrorCode.INVALID_PARAMETER, "Duplicate diet rule: " + code);
            DietThreshold override = new DietThreshold(request.limit(), request.caution()).validated(code.name());
            defaults.getOrDefault(code, new DietThreshold(null, null))
                    .overriddenBy(override.limit(), override.caution()).validated(code.name());
            prescription.setOverride(code, decimal(override.limit()), decimal(override.caution()));
        }
    }

    private static Map<DietRuleCode, DietThreshold> effective(NutritionDietPrescription p,
                                                              Map<DietRuleCode, DietThreshold> defaults) {
        Map<DietRuleCode, DietThreshold> result = new EnumMap<>(DietRuleCode.class);
        for (DietRuleCode code : DietRuleCode.values()) {
            BigDecimal[] override = p.getOverride(code);
            result.put(code, defaults.getOrDefault(code, new DietThreshold(null, null))
                    .overriddenBy(NutrientMapper.amount(override[0]), NutrientMapper.amount(override[1])));
        }
        return result;
    }

    private static String normalizeNote(String note) {
        if (note == null || note.isBlank()) return null;
        String trimmed = note.strip();
        if (trimmed.length() > MAX_NOTE_LENGTH)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "note must be at most " + MAX_NOTE_LENGTH + " characters");
        return trimmed;
    }

    private static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static NutritionDietPrescriptionResponse general(Long memberId, List<DietRuleResponse> definitions) {
        DietProfile general = DietProfile.general(Map.of());
        List<Rule> ruleList = definitions.stream().map(d -> {
            DietRuleCode code = DietRuleCode.valueOf(d.code());
            return new Rule(d.code(), d.name(), d.unit(), general.enabled(code), d.limit(), d.caution(), null, null,
                    d.limit(), d.caution());
        }).toList();
        return new NutritionDietPrescriptionResponse(memberId, false, general.limitSodium(), general.onWarfarin(),
                general.avoidAlcohol(), general.limitCaffeine(), null, null, null, null, ruleList);
    }

    private static NutritionDietPrescriptionResponse toResponse(NutritionDietPrescription p,
                                                                List<DietRuleResponse> definitions) {
        List<Rule> ruleList = definitions.stream().map(d -> {
            DietRuleCode code = DietRuleCode.valueOf(d.code());
            BigDecimal[] override = p.getOverride(code);
            Double limit = NutrientMapper.amount(override[0]);
            Double caution = NutrientMapper.amount(override[1]);
            DietThreshold effective = new DietThreshold(d.limit(), d.caution()).overriddenBy(limit, caution);
            return new Rule(d.code(), d.name(), d.unit(), p.isEnabled(code), d.limit(), d.caution(), limit, caution,
                    effective.limit(), effective.caution());
        }).toList();
        return new NutritionDietPrescriptionResponse(p.getMemberId(), true, p.isLimitSodium(), p.isOnWarfarin(),
                p.isAvoidAlcohol(), p.isLimitCaffeine(), p.getNote(), p.getPrescribedBy(), p.getConsultationSessionId(),
                p.getUpdatedAt(), ruleList);
    }
}
