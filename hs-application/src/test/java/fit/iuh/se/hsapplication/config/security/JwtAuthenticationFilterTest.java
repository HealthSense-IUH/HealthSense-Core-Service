package fit.iuh.se.hsapplication.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtDecoder jwtDecoder;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtDecoder = mock(JwtDecoder.class);
        filter = new JwtAuthenticationFilter(jwtDecoder, JsonMapper.builder().build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsStructuredJsonApiResponseOnInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/member/profile");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
        request.addHeader("lang", "en");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtDecoder.decode("expired-token")).thenThrow(new JwtException("expired"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"code\":1007");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void rejectsRefreshTokenUsedAsBearerAccessToken() throws Exception {
        Jwt refreshJwt = Jwt.withTokenValue("refresh-token")
                .header("alg", "none")
                .subject("10")
                .claim("type", "refresh")
                .claim("role", "MEMBER")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        when(jwtDecoder.decode("refresh-token")).thenReturn(refreshJwt);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/member/profile");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer refresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":1007");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void authenticatesValidAccessToken() throws Exception {
        Jwt accessJwt = Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .subject("99")
                .claim("type", "access")
                .claim("role", "MEMBER")
                .claim("email", "member@test.local")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        when(jwtDecoder.decode("valid-token")).thenReturn(accessJwt);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/member/profile");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsValidationOnPublicMobileLoginEvenWithStaleBearerHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/mobile/login");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-or-revoked-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtDecoder.decode("expired-or-revoked-token")).thenThrow(new JwtException("expired"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
    }
}
