package fit.iuh.se.hsapplication.config;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JpaAuditingConfigTest {

    private final JpaAuditingConfig config = new JpaAuditingConfig();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUserIdForUserAuthenticationPrincipal() {
        UserAuthentication principal = new UserAuthentication(2086670349981847552L, "mem01@gmail.com", UserRole.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));

        assertEquals("2086670349981847552", config.auditorProvider().getCurrentAuditor().orElseThrow());
    }

    @Test
    void fallsBackToEmailWhenUserIdIsNull() {
        UserAuthentication principal = new UserAuthentication(null, "mem01@gmail.com", UserRole.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));

        assertEquals("mem01@gmail.com", config.auditorProvider().getCurrentAuditor().orElseThrow());
    }

    @Test
    void returnsSystemWhenUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertEquals("SYSTEM", config.auditorProvider().getCurrentAuditor().orElseThrow());
    }
}
