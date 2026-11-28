package fit.iuh.se.hsassistant.service;

import fit.iuh.se.hsassistant.dto.MealEstimateResponse;
import fit.iuh.se.hsassistant.dto.MealEstimateResponse.MealItem;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.NutritionReferenceService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ước tính dinh dưỡng bữa ăn (ảnh món ăn và/hoặc mô tả văn bản) qua Spring AI Anthropic Claude Vision,
 * kết hợp đối chiếu bảng thành phần thực phẩm (VN_FCT / USDA) và đơn ăn uống tim mạch của hội viên.
 */
@Slf4j
@Service
public class MealEstimateService {

    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final String SYSTEM_PROMPT = """
            You are a clinical nutritionist assistant specializing in Vietnamese and international meals for cardiovascular & atrial fibrillation (AFib) patients.
            First determine whether the attached photo and/or description depicts or describes actual food, meals, ingredients, or beverages.
            If it is NOT food/drink (for example: a bill, receipt, text screenshot, document, person, object, scenery, or meaningless text like "test"), return ONLY valid JSON:
            {
              "isFood": false,
              "rejectionReason": "1 concise polite sentence in the requested language explaining that the input is not food or a beverage and asking the user to provide a meal photo or food description"
            }
            Otherwise, identify every food/drink item, estimate realistic portion weights in grams, and calculate calories, protein, carbohydrate, total fat, saturated fat, sugars, sodium (mg), and potassium (mg).
            Return ONLY valid JSON (no markdown fences, no commentary outside JSON) matching this exact schema:
            {
              "isFood": true,
              "mealName": "short concise meal title in the requested language",
              "confidence": "HIGH" | "MEDIUM" | "LOW",
              "totalGrams": number,
              "energyKcal": number,
              "proteinG": number,
              "carbohydrateG": number,
              "fatTotalG": number,
              "fatSaturatedG": number,
              "sugarsG": number,
              "sodiumMg": number,
              "potassiumMg": number,
              "cardiacNote": "1-2 concise sentences of cardiovascular/AFib guidance (mention sodium, potassium, or balance if relevant) in the requested language",
              "items": [
                {
                  "name": "item name in the requested language",
                  "estimatedGrams": number,
                  "energyKcal": number,
                  "proteinG": number,
                  "carbohydrateG": number,
                  "fatTotalG": number,
                  "sodiumMg": number,
                  "potassiumMg": number
                }
              ]
            }
            Ensure all numeric fields are non-negative numbers rounded to 1 decimal place. For Vietnamese soups/noodles (phở, bún bò, hủ tiếu, mì gói), account realistically for broth sodium.
            """;

    private final ChatClient chatClient;
    private final NutritionReferenceService referenceService;
    private final DietPrescriptionService dietPrescriptionService;
    private final ObjectMapper objectMapper;

    public MealEstimateService(ChatClient.Builder chatClientBuilder,
                               NutritionReferenceService referenceService,
                               DietPrescriptionService dietPrescriptionService,
                               ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.referenceService = referenceService;
        this.dietPrescriptionService = dietPrescriptionService;
        this.objectMapper = objectMapper;
    }

    public MealEstimateResponse estimate(Long userId,
                                         UserRole role,
                                         byte[] imageBytes,
                                         String mimeType,
                                         String description,
                                         String language) {
        boolean hasImage = imageBytes != null && imageBytes.length > 0;
        String cleanDesc = description == null ? "" : description.trim();
        boolean hasDescription = !cleanDesc.isEmpty();

        if (!hasImage && !hasDescription) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT,
                    "Please provide a meal photo or meal description.");
        }
        if (cleanDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT,
                    "Meal description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
        }

        String normalizedMime = null;
        if (hasImage) {
            if (imageBytes.length > MAX_IMAGE_BYTES) {
                throw new AppException(ErrorCode.INVALID_ARGUMENT,
                        "Meal image must not exceed 5 MB.");
            }
            normalizedMime = normalizeMime(mimeType);
            if (!ALLOWED_MIME_TYPES.contains(normalizedMime)) {
                throw new AppException(ErrorCode.INVALID_ARGUMENT,
                        "Unsupported image format. Please use JPEG, PNG, or WEBP.");
            }
        }

        boolean isEnglish = language != null && language.toLowerCase(Locale.ROOT).startsWith("en");
        String targetLang = isEnglish ? "English" : "Vietnamese";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Respond in ").append(targetLang).append(". ");
        if (hasImage && hasDescription) {
            userPrompt.append("Analyze the attached meal photo together with this user note: ").append(cleanDesc);
        } else if (hasImage) {
            userPrompt.append("Analyze the attached meal photo and estimate the nutritional breakdown.");
        } else {
            userPrompt.append("Estimate the nutritional breakdown for this meal: ").append(cleanDesc);
        }

        final String finalMime = "image/jpg".equals(normalizedMime) ? "image/jpeg" : normalizedMime;
        final String promptText = userPrompt.toString();

        String rawContent;
        try {
            rawContent = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(u -> {
                        u.text(promptText);
                        if (hasImage) {
                            u.media(MimeTypeUtils.parseMimeType(finalMime), new ByteArrayResource(imageBytes));
                        }
                    })
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("Meal estimation AI call failed for user {}: {}", userId, e.toString());
            throw new AppException(ErrorCode.UNCATEGORIZED,
                    isEnglish
                            ? "Unable to analyze the meal right now. Please try again later."
                            : "Hiện chưa thể phân tích bữa ăn. Vui lòng thử lại sau.");
        }

        DietProfile dietProfile = (userId != null && role == UserRole.MEMBER)
                ? dietPrescriptionService.profileOf(userId)
                : null;

        return parseAndEnrich(rawContent, cleanDesc, dietProfile, isEnglish);
    }

    MealEstimateResponse parseAndEnrich(String rawContent,
                                        String fallbackName,
                                        DietProfile dietProfile,
                                        boolean isEnglish) {
        String json = stripCodeFences(rawContent);
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("Failed to parse meal estimate JSON: {}", rawContent);
            throw new AppException(ErrorCode.UNCATEGORIZED,
                    isEnglish
                            ? "AI returned an unreadable nutrition estimate. Please try again."
                            : "Không đọc được kết quả ước tính dinh dưỡng. Vui lòng thử lại.");
        }

        if (root.has("isFood") && !root.path("isFood").asBoolean(true)) {
            String fallbackReason = isEnglish
                    ? "This photo or description does not appear to be food or a beverage. Please provide a meal photo or describe what you ate."
                    : "Ảnh hoặc mô tả này không phải là món ăn hay đồ uống. Vui lòng chọn ảnh bữa ăn hoặc mô tả thực phẩm cụ thể.";
            throw new AppException(ErrorCode.INVALID_ARGUMENT,
                    textOr(root.path("rejectionReason"), fallbackReason));
        }

        List<MealItem> items = new ArrayList<>();
        JsonNode itemsNode = root.path("items");
        if (itemsNode.isArray()) {
            for (JsonNode n : itemsNode) {
                String name = textOr(n.path("name"), "").trim();
                if (name.isEmpty()) continue;
                items.add(new MealItem(
                        name,
                        round1(nonNeg(n.path("estimatedGrams"))),
                        round1(nonNeg(n.path("energyKcal"))),
                        round1(nonNeg(n.path("proteinG"))),
                        round1(nonNeg(n.path("carbohydrateG"))),
                        round1(nonNeg(n.path("fatTotalG"))),
                        round1(nonNeg(n.path("sodiumMg"))),
                        round1(nonNeg(n.path("potassiumMg")))
                ));
            }
        }

        double totalGrams = round1(preferOrSum(root.path("totalGrams"), items.stream().mapToDouble(MealItem::estimatedGrams).sum()));
        double energyKcal = round1(preferOrSum(root.path("energyKcal"), items.stream().mapToDouble(MealItem::energyKcal).sum()));
        double proteinG = round1(preferOrSum(root.path("proteinG"), items.stream().mapToDouble(MealItem::proteinG).sum()));
        double carbG = round1(preferOrSum(root.path("carbohydrateG"), items.stream().mapToDouble(MealItem::carbohydrateG).sum()));
        double fatG = round1(preferOrSum(root.path("fatTotalG"), items.stream().mapToDouble(MealItem::fatTotalG).sum()));
        double satFatG = round1(nonNeg(root.path("fatSaturatedG")));
        double sugarsG = round1(nonNeg(root.path("sugarsG")));
        double sodiumMg = round1(preferOrSum(root.path("sodiumMg"), items.stream().mapToDouble(MealItem::sodiumMg).sum()));
        double potassiumMg = round1(preferOrSum(root.path("potassiumMg"), items.stream().mapToDouble(MealItem::potassiumMg).sum()));

        String defaultTitle = fallbackName.isEmpty()
                ? (isEnglish ? "Estimated Meal" : "Bữa ăn ước tính")
                : fallbackName;
        String mealName = textOr(root.path("mealName"), defaultTitle);
        String confidence = normalizeConfidence(textOr(root.path("confidence"), "MEDIUM"));
        String cardiacLevel = evaluateCardiacLevel(sodiumMg, potassiumMg, satFatG, sugarsG, dietProfile);
        String cardiacNote = textOr(root.path("cardiacNote"),
                defaultCardiacNote(cardiacLevel, sodiumMg, isEnglish));

        List<NutritionReferenceFoodSummaryResponse> matchedFoods = findMatchedFoods(items, mealName, dietProfile);

        return new MealEstimateResponse(
                mealName,
                confidence,
                totalGrams,
                energyKcal,
                proteinG,
                carbG,
                fatG,
                satFatG,
                sugarsG,
                sodiumMg,
                potassiumMg,
                cardiacLevel,
                cardiacNote,
                items,
                matchedFoods
        );
    }

    private List<NutritionReferenceFoodSummaryResponse> findMatchedFoods(List<MealItem> items,
                                                                         String mealName,
                                                                         DietProfile dietProfile) {
        Map<String, NutritionReferenceFoodSummaryResponse> unique = new LinkedHashMap<>();
        List<String> queries = new ArrayList<>();
        for (MealItem item : items) {
            if (queries.size() >= 4) break;
            queries.add(item.name());
        }
        if (queries.isEmpty() && mealName != null && !mealName.isBlank()) {
            queries.add(mealName);
        }

        for (String q : queries) {
            if (unique.size() >= 4) break;
            try {
                var page = referenceService.searchFoods(q, null, null, 1, 2, dietProfile);
                if (page != null && page.getContent() != null) {
                    for (NutritionReferenceFoodSummaryResponse food : page.getContent()) {
                        unique.putIfAbsent(food.id(), food);
                        if (unique.size() >= 4) break;
                    }
                }
            } catch (Exception e) {
                log.debug("Reference food lookup skipped for '{}': {}", q, e.toString());
            }
        }
        return List.copyOf(unique.values());
    }

    private static String evaluateCardiacLevel(double sodiumMg,
                                               double potassiumMg,
                                               double satFatG,
                                               double sugarsG,
                                               DietProfile profile) {
        boolean strictSodium = profile != null && profile.enabled(DietRuleCode.SODIUM);
        double sodiumLimit = strictSodium ? 800.0 : 1000.0;
        double sodiumCaution = strictSodium ? 500.0 : 650.0;

        if (sodiumMg >= sodiumLimit || satFatG >= 15.0 || sugarsG >= 35.0) {
            return "LIMIT";
        }
        if (sodiumMg >= sodiumCaution || satFatG >= 10.0 || sugarsG >= 22.0) {
            return "CAUTION";
        }
        if (sodiumMg <= 450.0 && potassiumMg >= 400.0 && satFatG <= 6.0) {
            return "GOOD";
        }
        return "OK";
    }

    private static String defaultCardiacNote(String level, double sodiumMg, boolean isEnglish) {
        return switch (level) {
            case "LIMIT" -> isEnglish
                    ? "High sodium (~" + Math.round(sodiumMg) + " mg) or saturated fat/sugar for a single meal. Consider reducing salty broth or sauces."
                    : "Bữa ăn có lượng natri (~" + Math.round(sodiumMg) + " mg), đường hoặc chất béo bão hòa khá cao. Nên bớt nước dùng mặn hoặc nước chấm.";
            case "CAUTION" -> isEnglish
                    ? "Moderate sodium (~" + Math.round(sodiumMg) + " mg). Balance the rest of your day to stay under the 2,000 mg AHA daily limit."
                    : "Lượng natri ở mức trung bình (~" + Math.round(sodiumMg) + " mg). Nên cân đối các bữa còn lại để giữ tổng natri dưới 2.000 mg/ngày.";
            case "GOOD" -> isEnglish
                    ? "Heart-friendly balance with controlled sodium and good potassium support."
                    : "Bữa ăn cân đối, ít muối và có kali hỗ trợ tốt cho sức khỏe tim mạch.";
            default -> isEnglish
                    ? "Reasonable nutrition profile for a standard meal."
                    : "Thành phần dinh dưỡng ở mức hợp lý cho một bữa ăn thông thường.";
        };
    }

    private static String normalizeMime(String mimeType) {
        if (mimeType == null) return "";
        int semi = mimeType.indexOf(';');
        String base = semi >= 0 ? mimeType.substring(0, semi) : mimeType;
        return base.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeConfidence(String value) {
        String upper = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "HIGH", "MEDIUM", "LOW" -> upper;
            default -> "MEDIUM";
        };
    }

    private static String stripCodeFences(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed.trim();
    }

    private static String textOr(JsonNode node, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) return fallback;
        String val = node.asText("");
        return val.isBlank() ? fallback : val;
    }

    private static double nonNeg(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return 0.0;
        double val = node.asDouble(0.0);
        return Double.isFinite(val) && val > 0 ? val : 0.0;
    }

    private static double preferOrSum(JsonNode node, double sum) {
        double val = nonNeg(node);
        return val > 0 ? val : Math.max(0.0, sum);
    }

    private static double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
