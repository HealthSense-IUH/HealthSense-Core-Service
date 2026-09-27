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
import fit.iuh.se.hsnutrition.repository.NutritionGuidanceFoodRepository;
import fit.iuh.se.hsnutrition.service.NutritionCatalogService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    @Override
    public List<NutritionFoodGroupResponse> getGroups() {
        Map<String, Long> counts = foods.countByGroup().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        return groups.findAllByOrderByDisplayOrderAsc().stream()
                .map(group -> toResponse(group, counts.getOrDefault(group.getId(), 0L)))
                .toList();
    }

    @Override
    public NutritionFoodGroupResponse getGroup(String idOrSlug) {
        NutritionFoodGroup group = findGroup(idOrSlug);
        return toResponse(group, foods.countByGroupId(group.getId()));
    }

    @Override
    public List<NutritionFoodResponse> getGroupFoods(String idOrSlug) {
        return foods.findByGroupIdOrderByDisplayOrderAsc(findGroup(idOrSlug).getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public NutritionFoodResponse getFood(String id) {
        return foods.findWithDetailsById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Nutrition food not found"));
    }

    @Override
    public List<NutritionFoodResponse> searchFoods(String query) {
        if (query == null || query.isBlank()) return List.of();
        if (query.length() > MAX_QUERY_LENGTH)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "q must be at most " + MAX_QUERY_LENGTH + " characters");
        String escaped = normalizeForSearch(query)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        // Không phân trang trong SQL: truy vấn fetch kèm danh sách bằng chứng, Hibernate sẽ phải cắt trong bộ nhớ.
        return foods.search("%" + escaped + "%").stream()
                .limit(MAX_SEARCH_RESULTS)
                .map(this::toResponse)
                .toList();
    }

    /** Phải khớp cách V22 tạo cột search_text: chữ thường, bỏ dấu, đ -> d, gộp khoảng trắng. */
    static String normalizeForSearch(String text) {
        String withoutMarks = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{Mn}+", "")
                .replace('đ', 'd');
        return String.join(" ", withoutMarks.trim().split("\\s+"));
    }

    private NutritionFoodGroup findGroup(String idOrSlug) {
        return groups.findById(idOrSlug)
                .or(() -> groups.findBySlug(idOrSlug))
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Nutrition food group not found"));
    }

    private NutritionFoodGroupResponse toResponse(NutritionFoodGroup group, long foodCount) {
        return new NutritionFoodGroupResponse(group.getId(), group.getSlug(), group.getName(), group.getDescription(),
                group.getDietaryPattern().name(), group.getIcon(), group.getImageUrl(), foodCount);
    }

    private NutritionFoodResponse toResponse(NutritionGuidanceFood item) {
        NutritionFood food = item.getFood();
        List<NutrientValue> nutrients = NutrientMapper.of(food);
        List<String> highlights = Arrays.stream(item.getHighlightNutrientCodes().split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
        List<EvidenceSource> evidence = item.getEvidenceSources().stream().map(this::toResponse).toList();
        return new NutritionFoodResponse(item.getId(), item.getGroup().getId(), item.getGroup().getName(),
                item.getFoodName(), item.getFoodNameSpecific(), item.getDescription(), item.getGuidance().name(),
                item.getGuidanceTitle(), item.getGuidanceReason(), item.getCardiovascularContext(),
                item.getAfContext(), item.getMedicationContext(), food.getSourceFoodCode(), food.getName(),
                PER_100_GRAMS, nutrients, highlights, evidence, item.getImageUrl());
    }

    private EvidenceSource toResponse(NutritionEvidenceSource source) {
        return new EvidenceSource(source.getId(), source.getTitle(), source.getSourceType().name(),
                source.getAuthors(), source.getJournal(), source.getPublicationYear(), source.getUrl(),
                source.getSummary());
    }
}
