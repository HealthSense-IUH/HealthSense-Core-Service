package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.NutritionFoodGroupResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.EvidenceSource;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.ServingReference;
import fit.iuh.se.hsnutrition.entity.NutritionEvidenceSource;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
import fit.iuh.se.hsnutrition.entity.NutritionFoodGroup;
import fit.iuh.se.hsnutrition.entity.NutritionGuidanceFood;
import fit.iuh.se.hsnutrition.repository.NutritionFoodGroupRepository;
import fit.iuh.se.hsnutrition.repository.NutritionFoodRepository;
import fit.iuh.se.hsnutrition.repository.NutritionGuidanceFoodRepository;
import fit.iuh.se.hsnutrition.service.NutritionCatalogService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.i18n.LocalizedText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionCatalogServiceImpl implements NutritionCatalogService {
    static final int MAX_QUERY_LENGTH = 100;
    static final int MAX_SEARCH_RESULTS = 20;
    static final ServingReference PER_100_GRAMS = new ServingReference(100, "g");

    private final NutritionFoodGroupRepository groups;
    private final NutritionGuidanceFoodRepository foods;
    private final NutritionFoodRepository referenceFoods;

    @Override
    public List<NutritionFoodGroupResponse> getGroups() {
        GroupCounts counts = countFoods();
        return groups.findAllByOrderByDisplayOrderAsc().stream().map(group -> toResponse(group, counts)).toList();
    }

    @Override
    public NutritionFoodGroupResponse getGroup(String idOrSlug) {
        return toResponse(findGroup(idOrSlug), countFoods());
    }

    @Override
    public List<NutritionFoodResponse> getGroupFoods(String idOrSlug) {
        return foods.findByGroupId(findGroup(idOrSlug).getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public NutritionFoodResponse getFood(String id) {
        return foods.findWithDetailsById(id)
                .map(this::toResponse)
                .orElseThrow(() -> AppException.of(ErrorCode.ENTITY_NOT_FOUND, "detail.nutrition-food-not-found"));
    }

    @Override
    public List<NutritionFoodResponse> searchFoods(String query) {
        if (query == null || query.isBlank()) return List.of();
        if (query.length() > MAX_QUERY_LENGTH)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.query-too-long", MAX_QUERY_LENGTH);
        String escaped = normalizeForSearch(query)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        // Không phân trang trong SQL: truy vấn fetch kèm danh sách bằng chứng, Hibernate sẽ phải cắt trong bộ nhớ.
        return foods.search("%" + escaped + "%").stream()
                .limit(MAX_SEARCH_RESULTS)
                .map(this::toResponse)
                .toList();
    }

    /**
     * Phải khớp cách V22 tạo cột search_text: chữ thường, bỏ dấu, đ -> d, gộp khoảng trắng.
     */
    static String normalizeForSearch(String text) {
        String withoutMarks = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{Mn}+", "")
                .replace('đ', 'd');
        return String.join(" ", withoutMarks.trim().split("\\s+"));
    }

    private NutritionFoodGroup findGroup(String idOrSlug) {
        return groups.findById(idOrSlug)
                .or(() -> groups.findBySlug(idOrSlug))
                .orElseThrow(() -> AppException.of(ErrorCode.ENTITY_NOT_FOUND, "detail.food-group-not-found"));
    }

    /**
     * Số thực phẩm theo nhóm và nguồn, và số món có khuyến nghị theo nhóm. Chỉ vài chục dòng nên đếm hết một lần.
     */
    private record GroupCounts(Map<String, Map<String, Long>> bySource, Map<String, Long> guidance) {
    }

    private GroupCounts countFoods() {
        Map<String, Map<String, Long>> bySource = new HashMap<>();
        for (Object[] row : referenceFoods.countByGroupAndSource())
            bySource.computeIfAbsent((String) row[0], id -> new LinkedHashMap<>()).put((String) row[1], (Long) row[2]);
        Map<String, Long> guidance = foods.countByGroup().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        return new GroupCounts(bySource, guidance);
    }

    private NutritionFoodGroupResponse toResponse(NutritionFoodGroup group, GroupCounts counts) {
        Map<String, Long> sources = counts.bySource().getOrDefault(group.getId(), Map.of());
        long total = sources.values().stream().mapToLong(Long::longValue).sum();
        return new NutritionFoodGroupResponse(group.getId(), group.getSlug(),
                LocalizedText.pick(group.getName(), group.getNameEn()),
                LocalizedText.pick(group.getDescription(), group.getDescriptionEn()),
                group.getIcon(), group.getImageUrl(), total, sources, counts.guidance().getOrDefault(group.getId(), 0L));
    }

    private NutritionFoodResponse toResponse(NutritionGuidanceFood item) {
        NutritionFood food = item.getFood();
        NutritionFoodGroup group = food.getGroup();
        List<NutrientValue> nutrients = NutrientMapper.of(food);
        List<String> highlights = Arrays.stream(item.getHighlightNutrientCodes().split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
        List<EvidenceSource> evidence = item.getEvidenceSources().stream().map(this::toResponse).toList();
        return new NutritionFoodResponse(item.getId(), group.getId(),
                LocalizedText.pick(group.getName(), group.getNameEn()),
                LocalizedText.pick(item.getFoodName(), item.getFoodNameEn()),
                LocalizedText.pick(item.getFoodNameSpecific(), item.getFoodNameSpecificEn()),
                LocalizedText.pick(item.getDescription(), item.getDescriptionEn()), item.getGuidance().name(),
                LocalizedText.pick(item.getGuidanceTitle(), item.getGuidanceTitleEn()),
                LocalizedText.pick(item.getGuidanceReason(), item.getGuidanceReasonEn()),
                LocalizedText.pick(item.getCardiovascularContext(), item.getCardiovascularContextEn()),
                LocalizedText.pick(item.getAfContext(), item.getAfContextEn()),
                LocalizedText.pick(item.getMedicationContext(), item.getMedicationContextEn()),
                food.getSourceFoodCode(), food.getName(),
                PER_100_GRAMS, nutrients, highlights, evidence, item.getImageUrl());
    }

    private EvidenceSource toResponse(NutritionEvidenceSource source) {
        return new EvidenceSource(source.getId(), source.getTitle(), source.getSourceType().name(),
                source.getAuthors(), source.getJournal(), source.getPublicationYear(), source.getUrl(),
                LocalizedText.pick(source.getSummary(), source.getSummaryEn()));
    }
}
