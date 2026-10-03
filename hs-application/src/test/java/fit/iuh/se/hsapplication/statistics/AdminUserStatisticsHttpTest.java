package fit.iuh.se.hsapplication.statistics;

import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.admin.statistics.AdminUserStatisticsController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;
import fit.iuh.se.hsuser.dto.response.statistics.UserStatisticsResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.service.statistics.UserStatisticsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserStatisticsHttpTest {

    AnnotationConfigWebApplicationContext context;
    MockMvc mvc;

    @BeforeEach
    void start() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "security.cors.allowed-origins=http://localhost:3000", "app.rate-limit.enabled=false");
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach
    void stop() {
        context.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({SecurityConfig.class, AdminUserStatisticsController.class, JwtAuthenticationFilter.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
    static class Config {
        @Bean
        UserStatisticsService statisticsService() {
            return mock(UserStatisticsService.class);
        }

        @Bean
        JwtDecoder decoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        RateLimiterService limiter() {
            return mock(RateLimiterService.class);
        }

        @Bean
        ObjectMapper mapper() {
            return JsonMapper.builder().build();
        }
    }

    @Test
    void rejectsAnonymousAndNonAdminRoles() throws Exception {
        var request = get("/api/admin/statistics/users")
                .param("from", "2026-09-01T00:00:00Z")
                .param("to", "2026-10-01T00:00:00Z");

        mvc.perform(request).andExpect(status().isUnauthorized());
        for (var role : List.of(UserRole.MEMBER, UserRole.DOCTOR, UserRole.CARE_COORDINATOR))
            mvc.perform(get("/api/admin/statistics/users")
                            .param("from", "2026-09-01T00:00:00Z")
                            .param("to", "2026-10-01T00:00:00Z")
                            .with(authentication(actor(role))))
                    .andExpect(status().isForbidden());

        verifyNoInteractions(context.getBean(UserStatisticsService.class));
    }

    @Test
    void adminReceivesStatisticsContractAndDefaultFilters() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        var response = new UserStatisticsResponse(
                new StatisticsPeriodResponse(from, to, "Asia/Ho_Chi_Minh", StatisticsGroupBy.DAY),
                new UserStatisticsResponse.Summary(120, 7, 108, 9),
                List.of(new StatisticsCountBucketResponse(LocalDate.parse("2026-09-01"), 3)),
                List.of(new UserStatisticsResponse.CategoryCount("MEMBER", 100)),
                List.of(new UserStatisticsResponse.CategoryCount("ACTIVE", 108)));
        when(context.getBean(UserStatisticsService.class).getStatistics(
                from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh")).thenReturn(response);

        mvc.perform(get("/api/admin/statistics/users")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(authentication(actor(UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalUsers").value(120))
                .andExpect(jsonPath("$.data.summary.activeAccounts").value(108))
                .andExpect(jsonPath("$.data.registrationTrend[0].bucketStart").value("2026-09-01"))
                .andExpect(jsonPath("$.data.byRole[0].key").value("MEMBER"));

        verify(context.getBean(UserStatisticsService.class)).getStatistics(
                from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");
    }

    @Test
    void superAdminCanSelectGroupingAndTimezone() throws Exception {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-07-01T00:00:00Z");

        mvc.perform(get("/api/admin/statistics/users")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("groupBy", "MONTH")
                        .param("timezone", "UTC")
                        .with(authentication(actor(UserRole.SUPER_ADMIN))))
                .andExpect(status().isOk());

        verify(context.getBean(UserStatisticsService.class)).getStatistics(
                from, to, StatisticsGroupBy.MONTH, "UTC");
    }

    @Test
    void missingRequiredRangeIsBadRequest() throws Exception {
        when(context.getBean(UserStatisticsService.class).getStatistics(
                null, null, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"))
                .thenThrow(AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-from-required"));

        mvc.perform(get("/api/admin/statistics/users")
                        .with(authentication(actor(UserRole.ADMIN))))
                .andExpect(status().isBadRequest());
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        var principal = UserAuthentication.builder().userId(88L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
