package fit.iuh.se.hsapplication.statistics;

import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.doctor.DoctorStatisticsController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hschat.dto.response.statistics.DoctorConsultationStatisticsResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientDetailResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientListItemResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorStatisticsPeriodResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.service.statistics.DoctorStatisticsService;
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

import java.math.BigDecimal;
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

class DoctorStatisticsHttpTest {

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
    @Import({SecurityConfig.class, DoctorStatisticsController.class, JwtAuthenticationFilter.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
    static class Config {
        @Bean DoctorStatisticsService doctorStatisticsService() {
            return mock(DoctorStatisticsService.class);
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
    void rejectsAnonymousAndEveryNonDoctorRole() throws Exception {
        mvc.perform(get("/api/doctor/statistics/consultations")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-10-01T00:00:00Z"))
                .andExpect(status().isUnauthorized());
        for (var role : UserRole.values()) {
            if (role == UserRole.DOCTOR) continue;
            mvc.perform(get("/api/doctor/statistics/consultations")
                            .param("from", "2026-09-01T00:00:00Z")
                            .param("to", "2026-10-01T00:00:00Z")
                            .with(authentication(actor(role))))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(context.getBean(DoctorStatisticsService.class));
    }

    @Test
    void consultationStatisticsUsesDoctorIdentityFromToken() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        var response = new DoctorConsultationStatisticsResponse(
                new DoctorStatisticsPeriodResponse(from, to),
                new DoctorConsultationStatisticsResponse.Summary(
                        30, 10, 2, 1, 6, 1, new BigDecimal("60.00"), new BigDecimal("10.00")));
        when(service().getConsultationStatistics(123L, from, to)).thenReturn(response);

        mvc.perform(get("/api/doctor/statistics/consultations")
                        .param("doctorId", "999")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(authentication(actor(UserRole.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalConsultations").value(30))
                .andExpect(jsonPath("$.data.summary.completionRate").value(60.00));

        verify(service()).getConsultationStatistics(123L, from, to);
    }

    @Test
    void patientListForwardsOnlySupportedFiltersAndUsesOneBasedPaging() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        var pageable = PageRequest.of(1, 10);
        var patient = new DoctorPatientListItemResponse(
                88L,
                "Nguyen Van A",
                null,
                4,
                3,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-09-20T00:00:00Z"),
                ConsultationStatus.COMPLETED);
        when(service().getPatients(123L, from, to, "Nguyen", pageable))
                .thenReturn(new PageResponse<>(new PageImpl<>(List.of(patient), pageable, 11)));

        mvc.perform(get("/api/doctor/statistics/patients")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("keyword", "Nguyen")
                        .param("page", "2")
                        .param("size", "10")
                        .with(authentication(actor(UserRole.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.content[0].memberId").value("88"))
                .andExpect(jsonPath("$.data.content[0].latestConsultationStatus").value("COMPLETED"));

        verify(service()).getPatients(123L, from, to, "Nguyen", pageable);
    }

    @Test
    void doctorCanRequestPatientDetailWithinServiceAuthorizationBoundary() throws Exception {
        var detail = new DoctorPatientDetailResponse(
                88L, "member@example.com", "Nguyen Van A", "0900000000",
                LocalDate.parse("1990-01-01"), "MALE", null, "HCM", "Asia/Ho_Chi_Minh", "HI-1");
        when(service().getPatient(123L, 88L)).thenReturn(detail);

        mvc.perform(get("/api/doctor/statistics/patients/88")
                        .with(authentication(actor(UserRole.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value("88"))
                .andExpect(jsonPath("$.data.displayName").value("Nguyen Van A"));

        verify(service()).getPatient(123L, 88L);
    }

    private DoctorStatisticsService service() {
        return context.getBean(DoctorStatisticsService.class);
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        var principal = UserAuthentication.builder().userId(123L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
