package fit.iuh.se.hsauth.service.token.impl;

import fit.iuh.se.hsauth.dto.token.PasswordResetTokenClaims;
import fit.iuh.se.hsauth.service.token.PasswordResetTokenService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final String TOKEN_TYPE = "password_reset";

    JwtEncoder jwtEncoder;
    JwtDecoder jwtDecoder;

    @NonFinal
    @Value("${security.jwt.issuer}")
    String issuer;

    @NonFinal
    @Value("${app.notification.reset-token.ttl}")
    Duration resetTokenTtl;

    @Override
    public String generatePasswordResetToken(UserAccount user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(resetTokenTtl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("type", TOKEN_TYPE)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public PasswordResetTokenClaims validatePasswordResetToken(String resetToken) {
        try {
            Jwt jwt = jwtDecoder.decode(resetToken);
            String type = jwt.getClaimAsString("type");
            String email = jwt.getClaimAsString("email");

            if (!TOKEN_TYPE.equals(type) || email == null || email.isBlank())
                throw new AppException(ErrorCode.INVALID_TOKEN);

            return new PasswordResetTokenClaims(
                    Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                    email,
                    jwt.getId()
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}
