package fit.iuh.se.hsapplication.config.init;

import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.entity.UserSensitiveData;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SuperAdminInitConfig implements ApplicationRunner {

    UserAccountRepository userAccountRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${app.bootstrap.super-admin.email:}")
    String email;

    @NonFinal
    @Value("${app.bootstrap.super-admin.password:}")
    String password;

    @NonFinal
    @Value("${app.bootstrap.super-admin.full-name:System Super Admin}")
    String fullName;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (userAccountRepository.existsByRoleAndStatus(UserRole.SUPER_ADMIN, AccountStatus.ACTIVE))
            return;

        validateBootstrapProperties();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userAccountRepository.existsByEmail(normalizedEmail))
            throw new IllegalStateException("Super admin email already exists");

        UserAccount superAdmin = UserAccount.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.SUPER_ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(superAdmin)
                .displayName(fullName.trim())
                .build();

        UserSensitiveData sensitiveData = UserSensitiveData.builder()
                .user(superAdmin)
                .build();

        superAdmin.setProfile(profile);
        superAdmin.setSensitiveData(sensitiveData);
        userAccountRepository.save(superAdmin);

        log.info("Bootstrapped initial SUPER_ADMIN account with email {}", normalizedEmail);
    }

    private void validateBootstrapProperties() {
        if (email == null || email.isBlank())
            throw new IllegalStateException("Missing app.bootstrap.super-admin.email for initial SUPER_ADMIN bootstrap");
        if (password == null || password.isBlank())
            throw new IllegalStateException("Missing app.bootstrap.super-admin.password for initial SUPER_ADMIN bootstrap");
        if (fullName == null || fullName.isBlank())
            throw new IllegalStateException("Missing app.bootstrap.super-admin.full-name for initial SUPER_ADMIN bootstrap");
    }
}
