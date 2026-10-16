package fit.iuh.se.hsapplication.statistics;

import fit.iuh.se.hsbilling.dto.CreditWalletResponse;
import fit.iuh.se.hsbilling.dto.statistics.MemberPaymentStatisticsResponse;
import fit.iuh.se.hsbilling.service.statistics.MemberPaymentStatisticsService;
import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.member.statistics.MemberHealthRecordStatisticsController;
import fit.iuh.se.hsapplication.controller.member.statistics.MemberPaymentStatisticsController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hshealthrecord.dto.response.statistics.MemberHealthRecordStatisticsResponse;
import fit.iuh.se.hshealthrecord.service.statistics.HealthRecordStatisticsService;
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

class MemberPaymentAndHealthStatisticsHttpTest {

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
    @Import({SecurityConfig.class, MemberPaymentStatisticsController.class,
            MemberHealthRecordStatisticsController.class, JwtAuthenticationFilter.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
    static class Config {
        @Bean MemberPaymentStatisticsService paymentStatistics() {
            return mock(MemberPaymentStatisticsService.class);
        }

        @Bean HealthRecordStatisticsService healthStatistics() {
            return mock(HealthRecordStatisticsService.class);
        }

        @Bean JwtDecoder decoder() {
            return mock(JwtDecoder.class);
        }

        @Bean RateLimiterService limiter() {
            return mock(RateLimiterService.class);
        }

        @Bean ObjectMapper mapper() {
            return JsonMapper.builder().build();
        }
    }

    @Test
    void rejectsAnonymousAndEveryNonMemberRoleForBothEndpoints() throws Exception {
        for (String path : List.of("payments", "health-records")) {
            mvc.perform(get("/api/member/statistics/" + path)
                            .param("from", "2026-09-01T00:00:00Z")
                            .param("to", "2026-10-01T00:00:00Z"))
                    .andExpect(status().isUnauthorized());
            for (var role : UserRole.values()) {
                if (role == UserRole.MEMBER) continue;
                mvc.perform(get("/api/member/statistics/" + path)
                                .param("from", "2026-09-01T00:00:00Z")
                                .param("to", "2026-10-01T00:00:00Z")
                                .with(authentication(actor(role))))
                        .andExpect(status().isForbidden());
            }
        }

        verifyNoInteractions(context.getBean(MemberPaymentStatisticsService.class));
        verifyNoInteractions(context.getBean(HealthRecordStatisticsService.class));
    }

    @Test
    void paymentStatisticsUsesMemberIdentityFromToken() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        var response = new MemberPaymentStatisticsResponse(
                new StatisticsPeriodResponse(from, to, "Asia/Ho_Chi_Minh", StatisticsGroupBy.DAY),
                new MemberPaymentStatisticsResponse.Summary(500_000, 150_000, 50, 15, 6, 2, 3, 75_000),
                new CreditWalletResponse(20, 2, 18),
                List.of(new MemberPaymentStatisticsResponse.PaymentBucket(
                        LocalDate.parse("2026-09-01"), 150_000, 15, 2)),
                List.of(new MemberPaymentStatisticsResponse.CategoryCount("PAID", 2)));
        when(context.getBean(MemberPaymentStatisticsService.class).getStatistics(
                123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh")).thenReturn(response);

        mvc.perform(get("/api/member/statistics/payments")
                        .param("memberId", "999")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalPaidVnd").value(500000))
                .andExpect(jsonPath("$.data.wallet.available").value(18))
                .andExpect(jsonPath("$.data.paymentTrend[0].purchasedCredits").value(15));

        verify(context.getBean(MemberPaymentStatisticsService.class)).getStatistics(
                123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");
    }

    @Test
    void healthStatisticsUsesMemberIdentityFromToken() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        Instant latest = Instant.parse("2026-09-20T02:00:00Z");
        var response = new MemberHealthRecordStatisticsResponse(
                new StatisticsPeriodResponse(from, to, "Asia/Ho_Chi_Minh", StatisticsGroupBy.DAY),
                new MemberHealthRecordStatisticsResponse.Summary(8, 3, 2, 1, 1, latest),
                List.of(new StatisticsCountBucketResponse(LocalDate.parse("2026-09-01"), 3)),
                List.of(new MemberHealthRecordStatisticsResponse.CategoryCount("AFIB_SUSPECTED", 1)),
                List.of(new MemberHealthRecordStatisticsResponse.CategoryCount("COMPLETED", 2)),
                List.of(new MemberHealthRecordStatisticsResponse.HourCount(9, 3)));
        when(context.getBean(HealthRecordStatisticsService.class).getMemberStatistics(
                123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh")).thenReturn(response);

        mvc.perform(get("/api/member/statistics/health-records")
                        .param("memberId", "999")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalRecords").value(8))
                .andExpect(jsonPath("$.data.summary.latestRecordAt").value(latest.toString()))
                .andExpect(jsonPath("$.data.resultDistribution[0].key").value("AFIB_SUSPECTED"));

        verify(context.getBean(HealthRecordStatisticsService.class)).getMemberStatistics(
                123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        var principal = UserAuthentication.builder().userId(123L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
