package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse.Portion;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
import fit.iuh.se.hsnutrition.entity.NutritionFoodGroup;
import fit.iuh.se.hsnutrition.repository.NutritionFoodGroupRepository;
import fit.iuh.se.hsnutrition.repository.NutritionFoodPortionRepository;
import fit.iuh.se.hsnutrition.repository.NutritionFoodRepository;
import fit.iuh.se.hsnutrition.service.NutritionReferenceService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionReferenceServiceImpl implements NutritionReferenceService {
    static final int MAX_PAGE_SIZE = 50;
    static final int MAX_QUERY_LENGTH = 100;
    static final int MAX_QUERY_TOKENS = 8;
    static final Set<String> SOURCES = Set.of(SOURCE_USDA, SOURCE_VIETNAM);

    private final NutritionFoodRepository foods;
    private final NutritionFoodPortionRepository portions;
    private final NutritionFoodGroupRepository groups;

    @Override
    public PageResponse<NutritionReferenceFoodSummaryResponse> searchFoods(String query, String group, String source,
                                                                           int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE)
            throw new AppException(ErrorCode.INVALID_PARAMETER,
                    "page must be positive and size must be between 1 and " + MAX_PAGE_SIZE);
        if (query != null && query.length() > MAX_QUERY_LENGTH)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "q must be at most " + MAX_QUERY_LENGTH + " characters");
        String sourceFilter = source == null || source.isBlank() ? null : source.trim();
        if (sourceFilter != null && !SOURCES.contains(sourceFilter))
            throw new AppException(ErrorCode.INVALID_PARAMETER, "source must be one of " + SOURCES);

        Map<String, NutritionFoodGroup> allGroups = groups.findAll().stream()
                .collect(Collectors.toMap(NutritionFoodGroup::getId, Function.identity()));
        String groupFilter = resolveGroup(group, allGroups);

        Pageable pageable = PageRequest.of(page - 1, size);
        if (query == null || query.isBlank())
            return new PageResponse<>(foods.browse(groupFilter, sourceFilter, pageable)
                    .map(food -> toSummary(food, allGroups)));

        String tsquery = toPrefixTsQuery(query);
        // Chỉ toàn ký tự đặc biệt: không có gì để tìm, trả trang rỗng thay vì trả về mọi món.
        if (tsquery == null) return new PageResponse<>(new PageImpl<>(List.of(), pageable, 0));
        return new PageResponse<>(foods.search(tsquery, toPhrasePattern(query), groupFilter, sourceFilter, pageable)
                .map(food -> toSummary(food, allGroups)));
    }

    @Override
    public NutritionReferenceFoodResponse getFood(String id) {
        Long foodId = parseId(id);
        NutritionFood food = (foodId == null ? Optional.<NutritionFood>empty() : foods.findById(foodId))
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Nutrition reference food not found"));
        List<Portion> foodPortions = portions.findByFoodIdOrderBySequenceNumberAsc(food.getId()).stream()
                .map(p -> new Portion(p.getDescription(), NutrientMapper.amount(p.getGramWeight()), p.isDefaultPortion()))
                .toList();
        NutritionFoodGroup group = food.getGroup();
        return new NutritionReferenceFoodResponse(String.valueOf(food.getId()), food.getSourceFoodCode(),
                displayName(food), food.getNameVi(), group.getId(), group.getName(), food.getCategory(),
                food.getSource(), food.getSourceVersion(), NutrientMapper.amount(food.getWastePct()),
                NutrientMapper.of(food), foodPortions);
    }

    /** Nhận id ("FISH") hoặc slug ("fish-seafood") của nhóm; trống là không lọc. */
    private static String resolveGroup(String group, Map<String, NutritionFoodGroup> allGroups) {
        if (group == null || group.isBlank()) return null;
        String value = group.trim();
        if (allGroups.containsKey(value)) return value;
        return allGroups.values().stream().filter(g -> g.getSlug().equals(value)).map(NutritionFoodGroup::getId)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PARAMETER, "Unknown food group: " + value));
    }

    /**
     * Mẫu LIKE của cả cụm người dùng gõ, giữ dấu, chữ thường, gộp khoảng trắng; ký tự đặc biệt của LIKE
     * được thoát bằng '!'. Dùng để xếp món khớp đúng dấu ("phở") lên trước món chỉ khớp khi bỏ dấu ("phô mai").
     */
    static String toPhrasePattern(String query) {
        String phrase = String.join(" ", Normalizer.normalize(query, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT).trim().split("\\s+"));
        return "%" + phrase.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    /**
     * Dựng tsquery tìm theo tiền tố ("salm bak" -> "salm:* & bak:*"). Chỉ giữ chữ cái và chữ số sau khi
     * bỏ dấu, nên ký tự đặc biệt của cú pháp tsquery (&, |, !, :, ngoặc...) không bao giờ lọt vào truy vấn.
     */
    static String toPrefixTsQuery(String query) {
        List<String> tokens = Arrays.stream(NutritionCatalogServiceImpl.normalizeForSearch(query).split("[^a-z0-9]+"))
                .filter(token -> !token.isEmpty())
                .limit(MAX_QUERY_TOKENS)
                .toList();
        if (tokens.isEmpty()) return null;
        return tokens.stream().map(token -> token + ":*").collect(Collectors.joining(" & "));
    }

    /**
     * Tên hiển thị theo nguồn: thực phẩm Việt Nam hiện tên tiếng Việt; thực phẩm USDA giữ tên gốc tiếng Anh,
     * còn tên dịch (name_vi) trả riêng ở localName. Phải khớp với sắp xếp trong NutritionFoodRepository.
     */
    static String displayName(NutritionFood food) {
        return SOURCE_VIETNAM.equals(food.getSource()) && food.getNameVi() != null ? food.getNameVi() : food.getName();
    }

    private static Long parseId(String id) {
        try {
            return id == null ? null : Long.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private NutritionReferenceFoodSummaryResponse toSummary(NutritionFood food, Map<String, NutritionFoodGroup> allGroups) {
        // getId() của proxy lazy không truy vấn thêm; tên nhóm lấy từ danh sách nhóm đã nạp
        NutritionFoodGroup group = allGroups.get(food.getGroup().getId());
        return new NutritionReferenceFoodSummaryResponse(String.valueOf(food.getId()), food.getSource(),
                food.getSourceFoodCode(), displayName(food), food.getNameVi(), group.getId(), group.getName(),
                NutrientMapper.amount(food.getEnergyKcal()),
                NutrientMapper.amount(food.getProteinG()), NutrientMapper.amount(food.getCarbohydrateG()),
                NutrientMapper.amount(food.getFatTotalG()));
    }
}
