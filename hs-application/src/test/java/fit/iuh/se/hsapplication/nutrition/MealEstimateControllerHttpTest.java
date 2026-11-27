package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsapplication.config.security.*;
import fit.iuh.se.hsapplication.controller.nutrition.MealEstimateController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsassistant.dto.MealEstimateResponse;
import fit.iuh.se.hsassistant.dto.MealEstimateResponse.MealItem;
import fit.iuh.se.hsassistant.service.MealEstimateService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MealEstimateControllerHttpTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private MealEstimateService mealEstimateService;

    @BeforeEach
    void start() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "security.cors.allowed-origins=http://localhost:3000", "app.rate-limit.enabled=false");
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mealEstimateService = context.getBean(MealEstimateService.class);
    }

    @AfterEach
    void stop() {
        if (context != null) context.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({SecurityConfig.class, MealEstimateController.class, JwtAuthenticationFilter.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
    static class Config {
        @Bean
        MealEstimateService mealEstimateService() {
            return mock(MealEstimateService.class);
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
        var user = UserAuthentication.builder().userId(42L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private static MealEstimateResponse sampleResponse() {
        return new MealEstimateResponse(
                "Phở bò tái",
                "HIGH",
                450.0,
                430.0,
                26.5,
                58.0,
                9.5,
                3.2,
                4.0,
                1180.0,
                420.0,
                "LIMIT",
                "Nước dùng phở chứa khá nhiều natri (~1180 mg). Nên hạn chế húp hết nước dùng.",
                List.of(new MealItem("Phở bò tái", 450.0, 430.0, 26.5, 58.0, 9.5, 1180.0, 420.0)),
                List.of()
        );
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(post("/api/nutrition/estimate-meal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"1 tô phở bò\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void multipartImageAndNoteReturnsEstimateContract() throws Exception {
        when(mealEstimateService.estimate(eq(42L), eq(UserRole.MEMBER), any(byte[].class),
                eq("image/jpeg"), eq("1 tô phở bò"), eq("vi")))
                .thenReturn(sampleResponse());

        MockMultipartFile image = new MockMultipartFile(
                "image", "pho.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});

        mvc.perform(multipart("/api/nutrition/estimate-meal")
                        .file(image)
                        .param("description", "1 tô phở bò")
                        .header("lang", "vi")
                        .with(authentication(actor(UserRole.MEMBER)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.mealName").value("Phở bò tái"))
                .andExpect(jsonPath("$.data.energyKcal").value(430.0))
                .andExpect(jsonPath("$.data.sodiumMg").value(1180.0))
                .andExpect(jsonPath("$.data.cardiacLevel").value("LIMIT"))
                .andExpect(jsonPath("$.data.items[0].name").value("Phở bò tái"));
    }

    @Test
    void jsonTextDescriptionReturnsEstimate() throws Exception {
        when(mealEstimateService.estimate(eq(42L), eq(UserRole.MEMBER), isNull(),
                isNull(), eq("1 tô phở bò tái"), eq("vi")))
                .thenReturn(sampleResponse());

        mvc.perform(post("/api/nutrition/estimate-meal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("lang", "vi")
                        .content("{\"description\":\"1 tô phở bò tái\"}")
                        .with(authentication(actor(UserRole.MEMBER)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mealName").value("Phở bò tái"))
                .andExpect(jsonPath("$.data.confidence").value("HIGH"));
    }

    @Test
    void emptyInputReturnsBadRequest() throws Exception {
        when(mealEstimateService.estimate(anyLong(), any(), isNull(), isNull(), isNull(), any()))
                .thenThrow(new AppException(ErrorCode.INVALID_ARGUMENT, "Please provide a meal photo or meal description."));

        mvc.perform(post("/api/nutrition/estimate-meal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(actor(UserRole.MEMBER)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1202));
    }
}
