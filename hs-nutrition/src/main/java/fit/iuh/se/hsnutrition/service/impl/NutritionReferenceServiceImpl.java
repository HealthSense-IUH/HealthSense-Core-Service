package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsnutrition.dto.NutritionReferenceCategoryResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse.Portion;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.entity.NutritionFood;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionReferenceServiceImpl implements NutritionReferenceService {
    static final int MAX_PAGE_SIZE = 50;
    static final int MAX_QUERY_LENGTH = 100;
    static final int MAX_CATEGORY_LENGTH = 160;
    static final int MAX_QUERY_TOKENS = 8;

    private final NutritionFoodRepository foods;
    private final NutritionFoodPortionRepository portions;

    @Override
    public PageResponse<NutritionReferenceFoodSummaryResponse> searchFoods(String query, String category, int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE)
            throw new AppException(ErrorCode.INVALID_PARAMETER,
                    "page must be positive and size must be between 1 and " + MAX_PAGE_SIZE);
        if (query != null && query.length() > MAX_QUERY_LENGTH)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "q must be at most " + MAX_QUERY_LENGTH + " characters");
        if (category != null && category.length() > MAX_CATEGORY_LENGTH)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "category is too long");

        Pageable pageable = PageRequest.of(page - 1, size);
        String categoryFilter = category == null || category.isBlank() ? null : category.trim();
        if (query == null || query.isBlank())
            return new PageResponse<>(foods.browse(categoryFilter, pageable).map(this::toSummary));

        String tsquery = toPrefixTsQuery(query);
        // Chỉ toàn ký tự đặc biệt: không có gì để tìm, trả trang rỗng thay vì trả về mọi món.
        if (tsquery == null) return new PageResponse<>(new PageImpl<>(List.of(), pageable, 0));
        return new PageResponse<>(foods.search(tsquery, categoryFilter, pageable).map(this::toSummary));
    }

    @Override
    public NutritionReferenceFoodResponse getFood(String id) {
        Long foodId = parseId(id);
        NutritionFood food = (foodId == null ? Optional.<NutritionFood>empty() : foods.findById(foodId))
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Nutrition reference food not found"));
        List<Portion> foodPortions = portions.findByFoodIdOrderBySequenceNumberAsc(food.getId()).stream()
                .map(p -> new Portion(p.getDescription(), NutrientMapper.amount(p.getGramWeight()), p.isDefaultPortion()))
                .toList();
        return new NutritionReferenceFoodResponse(String.valueOf(food.getId()), food.getSourceFoodCode(), food.getName(),
                food.getCategory(), food.getSource(), food.getSourceVersion(), NutrientMapper.of(food), foodPortions);
    }

    @Override
    public List<NutritionReferenceCategoryResponse> getCategories() {
        return foods.countByCategory().stream()
                .map(row -> new NutritionReferenceCategoryResponse((String) row[0], (Long) row[1]))
                .toList();
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

    private static Long parseId(String id) {
        try {
            return id == null ? null : Long.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private NutritionReferenceFoodSummaryResponse toSummary(NutritionFood food) {
        return new NutritionReferenceFoodSummaryResponse(String.valueOf(food.getId()), food.getSourceFoodCode(),
                food.getName(), food.getCategory(), NutrientMapper.amount(food.getEnergyKcal()),
                NutrientMapper.amount(food.getProteinG()), NutrientMapper.amount(food.getCarbohydrateG()),
                NutrientMapper.amount(food.getFatTotalG()));
    }
}
