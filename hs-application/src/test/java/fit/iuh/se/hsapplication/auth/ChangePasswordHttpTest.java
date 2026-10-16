package fit.iuh.se.hsapplication.auth;

import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.auth.DefaultAuthController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsauth.dto.request.ChangePasswordRequest;
import fit.iuh.se.hsauth.service.auth.AuthService;
import fit.iuh.se.hsauth.service.cookie.AuthCookieService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangePasswordHttpTest {

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

        AuthCookieService cookies = context.getBean(AuthCookieService.class);
        when(cookies.clearRefreshTokenCookie()).thenReturn(expiredCookie("refresh-token"));
        when(cookies.clearSessionIdCookie()).thenReturn(expiredCookie("session-id"));
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach
    void stop() {
        context.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({SecurityConfig.class, DefaultAuthController.class, JwtAuthenticationFilter.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
    static class Config {
        @Bean AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean AuthCookieService authCookieService() {
            return mock(AuthCookieService.class);
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
    void rejectsAnonymousRequest() throws Exception {
        mvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service());
    }

    @Test
    void allowsEveryAuthenticatedRoleAndUsesIdentityFromToken() throws Exception {
        for (UserRole role : UserRole.values()) {
            mvc.perform(post("/api/auth/change-password")
                            .with(authentication(actor(role)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.message").value("Password changed successfully"))
                    .andExpect(header().exists("Set-Cookie"));
        }

        verify(service(), org.mockito.Mockito.times(UserRole.values().length))
                .changePassword(org.mockito.ArgumentMatchers.eq(123L), any(ChangePasswordRequest.class));
    }

    @Test
    void rejectsWeakNewPasswordBeforeCallingService() throws Exception {
        mvc.perform(post("/api/auth/change-password")
                        .with(authentication(actor(UserRole.MEMBER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword123","newPassword":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1200));

        verifyNoInteractions(service());
    }

    private AuthService service() {
        return context.getBean(AuthService.class);
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        UserAuthentication user = new UserAuthentication(123L, "user@example.com", role);
        return UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private ResponseCookie expiredCookie(String name) {
        return ResponseCookie.from(name, "").path("/").maxAge(0).build();
    }

    private String validBody() {
        return """
                {"currentPassword":"OldPassword123","newPassword":"NewPassword456"}
                """;
    }
}
