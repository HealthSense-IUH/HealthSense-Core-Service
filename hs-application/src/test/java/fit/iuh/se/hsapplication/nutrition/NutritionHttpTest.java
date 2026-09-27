package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsapplication.config.security.*;
import fit.iuh.se.hsapplication.controller.nutrition.NutritionController;
import fit.iuh.se.hsapplication.controller.nutrition.NutritionReferenceController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsnutrition.dto.NutritionFoodGroupResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.EvidenceSource;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.ServingReference;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.service.NutritionCatalogService;
import fit.iuh.se.hsnutrition.service.NutritionReferenceService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The JSON field names asserted here are the Frontend contract in src/types/nutrition.ts.
 */
class NutritionHttpTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private NutritionCatalogService catalog;
    private NutritionReferenceService reference;

    @BeforeEach
    void start() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "security.cors.allowed-origins=http://localhost:3000", "app.rate-limit.enabled=false");
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        catalog = context.getBean(NutritionCatalogService.class);
        reference = context.getBean(NutritionReferenceService.class);
    }

    @AfterEach
    void stop() {
        if (context != null) context.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({SecurityConfig.class, NutritionController.class, NutritionReferenceController.class, JwtAuthenticationFilter.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
    static class Config {
        @Bean
        NutritionCatalogService catalog() {
            return mock(NutritionCatalogService.class);
        }

        @Bean
        NutritionReferenceService reference() {
            return mock(NutritionReferenceService.class);
        }

        @Bean
        JwtDecoder decoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        RateLimiterService rateLimiter() {
            return mock(RateLimiterService.class);
        }

        @Bean
        ObjectMapper mapper() {
            return JsonMapper.builder().build();
        }
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        var user = UserAuthentication.builder().userId(123L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private static NutritionFoodResponse milk() {
        return new NutritionFoodResponse("milk-low-fat-1", "DAIRY", "Sữa và sản phẩm từ sữa", "Sữa", "Sữa ít béo 1%",
                "mô tả", "PRIORITIZE", "tiêu đề", "lý do", "tim mạch", null, null, "11112210",
                "Milk, low fat (1%)", new ServingReference(100, "g"),
                List.of(new NutrientValue("energy", "Năng lượng", 43, "kcal", true),
                        new NutrientValue("fiber", "Chất xơ tiêu hóa", 0, "g", false)),
                List.of("protein", "sodium"),
                List.of(new EvidenceSource("acc-aha-2023", "Guideline", "GUIDELINE", null, "Circulation", 2023,
                        "https://example.org", null)),
                null);
    }

    @Test
    void anonymousIsRejectedAndEveryRoleCanRead() throws Exception {
        when(catalog.getGroups()).thenReturn(List.of());
        for (String path : List.of("/api/nutrition/groups", "/api/nutrition/groups/FISH/foods",
                "/api/nutrition/foods/milk-low-fat-1", "/api/nutrition/foods/search?q=sua"))
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        for (UserRole role : UserRole.values())
            mvc.perform(get("/api/nutrition/groups").with(authentication(actor(role))))
                    .andExpect(status().isOk());
    }

    @Test
    void foodJsonUsesTheFrontendFieldNames() throws Exception {
        when(catalog.getFood("milk-low-fat-1")).thenReturn(milk());
        mvc.perform(get("/api/nutrition/foods/milk-low-fat-1").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.id").value("milk-low-fat-1"))
                .andExpect(jsonPath("$.data.group").value("DAIRY"))
                .andExpect(jsonPath("$.data.foodNameSpecific").value("Sữa ít béo 1%"))
                .andExpect(jsonPath("$.data.sourceFoodCode").value("11112210"))
                .andExpect(jsonPath("$.data.servingReference.amount").value(100))
                .andExpect(jsonPath("$.data.servingReference.unit").value("g"))
                .andExpect(jsonPath("$.data.nutrients[0].nutrientCode").value("energy"))
                .andExpect(jsonPath("$.data.nutrients[0].amount").value(43.0))
                .andExpect(jsonPath("$.data.nutrients[0].isKey").value(true))
                .andExpect(jsonPath("$.data.nutrients[1].isKey").value(false))
                .andExpect(jsonPath("$.data.nutrients[0].key").doesNotExist())
                .andExpect(jsonPath("$.data.highlightNutrientCodes[1]").value("sodium"))
                .andExpect(jsonPath("$.data.evidenceSources[0].sourceType").value("GUIDELINE"))
                .andExpect(jsonPath("$.data.evidenceSources[0].year").value(2023))
                // Optional fields are omitted, matching `field?: string` in TypeScript
                .andExpect(jsonPath("$.data.afContext").doesNotExist())
                .andExpect(jsonPath("$.data.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.data.evidenceSources[0].authors").doesNotExist());
    }

    @Test
    void groupJsonCarriesFoodCountsPerSource() throws Exception {
        Map<String, Long> sources = new LinkedHashMap<>();
        sources.put("VN_FCT", 61L);
        sources.put("USDA_FNDDS", 152L);
        when(catalog.getGroups()).thenReturn(List.of(new NutritionFoodGroupResponse(
                "FISH", "fish-seafood", "Cá và hải sản", "mô tả", "Fish", null, 213, sources, 4)));
        mvc.perform(get("/api/nutrition/groups").with(authentication(actor(UserRole.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].slug").value("fish-seafood"))
                .andExpect(jsonPath("$.data[0].icon").value("Fish"))
                .andExpect(jsonPath("$.data[0].foodCount").value(213))
                .andExpect(jsonPath("$.data[0].sourceCounts.VN_FCT").value(61))
                .andExpect(jsonPath("$.data[0].sourceCounts.USDA_FNDDS").value(152))
                .andExpect(jsonPath("$.data[0].guidanceFoodCount").value(4))
                .andExpect(jsonPath("$.data[0].dietaryPattern").doesNotExist())
                .andExpect(jsonPath("$.data[0].imageUrl").doesNotExist());
    }

    @Test
    void searchPathIsNotMistakenForAFoodId() throws Exception {
        when(catalog.searchFoods("ca hoi")).thenReturn(List.of(milk()));
        mvc.perform(get("/api/nutrition/foods/search").param("q", "ca hoi")
                        .with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        verify(catalog).searchFoods("ca hoi");
        verify(catalog, never()).getFood(anyString());
    }

    @Test
    void unknownFoodIsNotFound() throws Exception {
        when(catalog.getFood("nope")).thenThrow(new AppException(ErrorCode.ENTITY_NOT_FOUND, "Nutrition food not found"));
        mvc.perform(get("/api/nutrition/foods/nope").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(3000));
    }

    @Test
    void referenceEndpointsRequireLoginAndUseDefaultPaging() throws Exception {
        for (String path : List.of("/api/nutrition/reference/foods", "/api/nutrition/reference/foods/2707124"))
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        when(reference.searchFoods("", null, null, 1, 20)).thenReturn(new PageResponse<>(new PageImpl<>(List.of(),
                PageRequest.of(0, 20), 0)));
        mvc.perform(get("/api/nutrition/reference/foods").with(authentication(actor(UserRole.DOCTOR))))
                .andExpect(status().isOk());
        verify(reference).searchFoods("", null, null, 1, 20);
    }

    @Test
    void referenceSearchJsonIsAPageOfSummaries() throws Exception {
        var pho = new NutritionReferenceFoodSummaryResponse("2707124", "USDA_FNDDS", "28310330",
                "Soup, pho, with meat", "Phở có thịt", "MIXED_DISH", "Món ăn hỗn hợp", 77.0, 5.81, 5.6, null);
        when(reference.searchFoods("pho", "MIXED_DISH", "USDA_FNDDS", 2, 10)).thenReturn(
                new PageResponse<>(new PageImpl<>(List.of(pho), PageRequest.of(1, 10), 11)));
        mvc.perform(get("/api/nutrition/reference/foods").param("q", "pho")
                        .param("group", "MIXED_DISH").param("source", "USDA_FNDDS").param("page", "2").param("size", "10")
                        .with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(11))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.hasMore").value(false))
                .andExpect(jsonPath("$.data.content[0].id").value("2707124"))
                .andExpect(jsonPath("$.data.content[0].sourceFoodCode").value("28310330"))
                .andExpect(jsonPath("$.data.content[0].source").value("USDA_FNDDS"))
                .andExpect(jsonPath("$.data.content[0].displayName").value("Soup, pho, with meat"))
                .andExpect(jsonPath("$.data.content[0].localName").value("Phở có thịt"))
                .andExpect(jsonPath("$.data.content[0].group").value("MIXED_DISH"))
                .andExpect(jsonPath("$.data.content[0].groupName").value("Món ăn hỗn hợp"))
                .andExpect(jsonPath("$.data.content[0].category").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].name").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].energyKcal").value(77.0))
                .andExpect(jsonPath("$.data.content[0].carbohydrateG").value(5.6))
                .andExpect(jsonPath("$.data.content[0].fatTotalG").doesNotExist());
    }

    @Test
    void referenceDetailJsonUsesTheFrontendFieldNames() throws Exception {
        when(reference.getFood("2707124")).thenReturn(new NutritionReferenceFoodResponse("2707124", "28310330",
                "Soup, pho, with meat", "Phở có thịt", "MIXED_DISH", "Món ăn hỗn hợp", "Ramen and Asian broth-based soups",
                "USDA_FNDDS", "2021-2023", 12.5,
                List.of(new NutrientValue("energy", "Năng lượng", 77, "kcal", true)),
                List.of(new NutritionReferenceFoodResponse.Portion("1 cup", 245, false),
                        new NutritionReferenceFoodResponse.Portion("Quantity not specified", 245, true))));
        mvc.perform(get("/api/nutrition/reference/foods/2707124").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceVersion").value("2021-2023"))
                .andExpect(jsonPath("$.data.displayName").value("Soup, pho, with meat"))
                .andExpect(jsonPath("$.data.localName").value("Phở có thịt"))
                .andExpect(jsonPath("$.data.group").value("MIXED_DISH"))
                .andExpect(jsonPath("$.data.sourceCategory").value("Ramen and Asian broth-based soups"))
                .andExpect(jsonPath("$.data.nameVi").doesNotExist())
                .andExpect(jsonPath("$.data.wastePct").value(12.5))
                .andExpect(jsonPath("$.data.nutrients[0].isKey").value(true))
                .andExpect(jsonPath("$.data.portions[0].description").value("1 cup"))
                .andExpect(jsonPath("$.data.portions[0].gramWeight").value(245.0))
                .andExpect(jsonPath("$.data.portions[1].isDefault").value(true))
                .andExpect(jsonPath("$.data.portions[1].default").doesNotExist());
    }
}
