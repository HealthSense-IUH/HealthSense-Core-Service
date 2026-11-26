package fit.iuh.se.hsapplication.config;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }
            if (authentication.getPrincipal() instanceof UserAuthentication user) {
                if (user.getUserId() != null) {
                    return Optional.of(user.getUserId().toString());
                }
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    return Optional.of(user.getEmail());
                }
            }
            return Optional.ofNullable(authentication.getName());
        };
    }
}
