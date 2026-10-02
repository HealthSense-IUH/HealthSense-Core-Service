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
import java.util.EnumSet;
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
        prescription.setLimitSugars(request.limitSugars());
        prescription.setWatchSodiumPotassium(request.watchSodiumPotassium());
        prescription.setLimitSaturatedFat(request.limitSaturatedFat());
        prescription.setEncourageMagnesium(request.encourageMagnesium());
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
                .map(p -> new DietProfile(prescribedRules(p), true, effective(p, defaults)))
                .orElseGet(() -> DietProfile.general(defaults));
    }

    private static Set<DietRuleCode> prescribedRules(NutritionDietPrescription p) {
        Set<DietRuleCode> result = EnumSet.noneOf(DietRuleCode.class);
        for (DietRuleCode code : DietRuleCode.values()) if (p.isEnabled(code)) result.add(code);
        return result;
    }

    /**
     * Ghi đè toàn bộ ngưỡng riêng: quy tắc không gửi lên thì về mặc định. Mỗi quy tắc chỉ nhận các mức nó có (Na/K: đỏ
     * và tốt; magie: tốt; còn lại: đỏ và vàng). Ngưỡng sau khi gộp với mặc định phải hợp lệ (đỏ không thấp hơn vàng,
     * mức tốt của Na/K không vượt ngưỡng đỏ), để bác sĩ không vô tình tạo cặp ngưỡng ngược nhau.
     */
    private void applyOverrides(NutritionDietPrescription prescription, List<DietThresholdRequest> thresholds) {
        Map<DietRuleCode, DietThreshold> defaults = rules.defaults();
        for (DietRuleCode code : DietRuleCode.values()) prescription.setOverride(code, null, null, null);
        if (thresholds == null) return;
        Set<DietRuleCode> seen = new HashSet<>();
        for (DietThresholdRequest request : thresholds) {
            DietRuleCode code = DietRuleService.parseCode(request.code());
            if (!seen.add(code)) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-rule-duplicate", code);
            if (!code.overridable())
                throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-rule-not-overridable", code);
            DietThreshold override = new DietThreshold(request.limit(), request.caution(), request.good())
                    .validated(code.name());
            boolean unusedGood = !code.usesGood() && override.good() != null;
            boolean unusedLimit = !code.usesLimitOrCaution() && override.limit() != null;
            boolean unusedCaution = (!code.usesLimitOrCaution() || code == DietRuleCode.NA_K_RATIO)
                    && override.caution() != null;
            if (unusedGood || unusedLimit || unusedCaution)
                throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-unused", code);
            DietThreshold merged = defaults.getOrDefault(code, new DietThreshold(null, null))
                    .overriddenBy(override.limit(), override.caution(), override.good()).validated(code.name());
            if (code == DietRuleCode.NA_K_RATIO && merged.good() != null && merged.limit() != null
                    && merged.good() > merged.limit())
                throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.diet-threshold-good-order", code,
                        merged.good(), merged.limit());
            prescription.setOverride(code, decimal(override.limit()), decimal(override.caution()),
                    decimal(override.good()));
        }
    }

    private static Map<DietRuleCode, DietThreshold> effective(NutritionDietPrescription p,
                                                              Map<DietRuleCode, DietThreshold> defaults) {
        Map<DietRuleCode, DietThreshold> result = new EnumMap<>(DietRuleCode.class);
        for (DietRuleCode code : DietRuleCode.values()) {
            BigDecimal[] override = p.getOverride(code);
            result.put(code, defaults.getOrDefault(code, new DietThreshold(null, null))
                    .overriddenBy(NutrientMapper.amount(override[0]), NutrientMapper.amount(override[1]),
                            NutrientMapper.amount(override[2])));
        }
        return result;
    }

    private static String normalizeNote(String note) {
        if (note == null || note.isBlank()) return null;
        String trimmed = note.strip();
        if (trimmed.length() > MAX_NOTE_LENGTH)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.note-too-long", MAX_NOTE_LENGTH);
        return trimmed;
    }

    private static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static NutritionDietPrescriptionResponse general(Long memberId, List<DietRuleResponse> definitions) {
        List<Rule> ruleList = definitions.stream().map(d -> {
            DietRuleCode code = DietRuleCode.valueOf(d.code());
            return new Rule(d.code(), d.name(), d.unit(), false, false, code.overridable(), d.limit(),
                    d.caution(), d.good(), null, null, null, d.limit(), d.caution(), d.good());
        }).toList();
        return new NutritionDietPrescriptionResponse(memberId, false, false, false, false, false, false, false, false,
                false, null, null, null, null, ruleList);
    }

    private static NutritionDietPrescriptionResponse toResponse(NutritionDietPrescription p,
                                                                List<DietRuleResponse> definitions) {
        List<Rule> ruleList = definitions.stream().map(d -> {
            DietRuleCode code = DietRuleCode.valueOf(d.code());
            BigDecimal[] override = p.getOverride(code);
            Double limit = NutrientMapper.amount(override[0]);
            Double caution = NutrientMapper.amount(override[1]);
            Double good = NutrientMapper.amount(override[2]);
            DietThreshold effective = new DietThreshold(d.limit(), d.caution(), d.good()).overriddenBy(limit, caution, good);
            boolean prescribed = p.isEnabled(code);
            return new Rule(d.code(), d.name(), d.unit(), prescribed, prescribed, code.overridable(),
                    d.limit(), d.caution(), d.good(), limit, caution, good, effective.limit(), effective.caution(),
                    effective.good());
        }).toList();
        return new NutritionDietPrescriptionResponse(p.getMemberId(), true, p.isLimitSodium(), p.isOnWarfarin(),
                p.isAvoidAlcohol(), p.isLimitCaffeine(), p.isLimitSugars(), p.isWatchSodiumPotassium(),
                p.isLimitSaturatedFat(), p.isEncourageMagnesium(), p.getNote(), p.getPrescribedBy(),
                p.getConsultationSessionId(), p.getUpdatedAt(), ruleList);
    }
}
