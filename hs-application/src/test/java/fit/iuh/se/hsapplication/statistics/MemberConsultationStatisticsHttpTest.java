package fit.iuh.se.hsapplication.statistics;

import fit.iuh.se.hschat.dto.response.statistics.ConsultationStatisticsResponse;
import fit.iuh.se.hschat.service.statistics.ConsultationStatisticsService;
import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.member.statistics.MemberConsultationStatisticsController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberConsultationStatisticsHttpTest {

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

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
    @Import({SecurityConfig.class, MemberConsultationStatisticsController.class, JwtAuthenticationFilter.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
    static class Config {
        @Bean
        ConsultationStatisticsService statisticsService() {
            return mock(ConsultationStatisticsService.class);
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
    void rejectsAnonymousAndEveryNonMemberRole() throws Exception {
        mvc.perform(get("/api/member/statistics/consultations")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-10-01T00:00:00Z"))
                .andExpect(status().isUnauthorized());

        for (var role : UserRole.values()) {
            if (role == UserRole.MEMBER) continue;
            mvc.perform(get("/api/member/statistics/consultations")
                            .param("from", "2026-09-01T00:00:00Z")
                            .param("to", "2026-10-01T00:00:00Z")
                            .with(authentication(actor(role))))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(context.getBean(ConsultationStatisticsService.class));
    }

    @Test
    void usesMemberIdentityFromTokenAndReturnsStatisticsContract() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        var response = new ConsultationStatisticsResponse(
                new StatisticsPeriodResponse(from, to, "Asia/Ho_Chi_Minh", StatisticsGroupBy.DAY),
                new ConsultationStatisticsResponse.Summary(12, 3, 2, 1),
                List.of(new StatisticsCountBucketResponse(LocalDate.parse("2026-09-01"), 1)),
                List.of(new ConsultationStatisticsResponse.CategoryCount("COMPLETED", 2)));
        when(context.getBean(ConsultationStatisticsService.class).getMemberStatistics(
                123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh")).thenReturn(response);

        mvc.perform(get("/api/member/statistics/consultations")
                        .param("memberId", "999")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalConsultations").value(12))
                .andExpect(jsonPath("$.data.summary.consultationsInPeriod").value(3))
                .andExpect(jsonPath("$.data.consultationTrend[0].bucketStart").value("2026-09-01"))
                .andExpect(jsonPath("$.data.byStatus[0].key").value("COMPLETED"));

        verify(context.getBean(ConsultationStatisticsService.class)).getMemberStatistics(
                123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");
    }

    @Test
    void memberCanSelectGroupingAndTimezone() throws Exception {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-07-01T00:00:00Z");

        mvc.perform(get("/api/member/statistics/consultations")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("groupBy", "MONTH")
                        .param("timezone", "UTC")
                        .with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk());

        verify(context.getBean(ConsultationStatisticsService.class)).getMemberStatistics(
                123L, from, to, StatisticsGroupBy.MONTH, "UTC");
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        var principal = UserAuthentication.builder().userId(123L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
