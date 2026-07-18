package fit.iuh.se.hsauth.service.token.impl;

import fit.iuh.se.hsauth.dto.token.RefreshTokenClaims;
import fit.iuh.se.hsauth.service.token.RefreshTokenService;
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
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String TOKEN_TYPE = "refresh";

    JwtEncoder jwtEncoder;
    JwtDecoder jwtDecoder;

    @NonFinal
    @Value("${security.jwt.issuer}")
    String issuer;

    @NonFinal
    @Value("${security.jwt.refresh-token-ttl}")
    Duration refreshTokenTtl;

    @Override
    public String generateRefreshToken(UserAccount user, String sessionId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(refreshTokenTtl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim("sid", sessionId)
                .claim("type", TOKEN_TYPE)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public RefreshTokenClaims validateRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String type = jwt.getClaimAsString("type");
            String sessionId = jwt.getClaimAsString("sid");

            if (!TOKEN_TYPE.equals(type) || sessionId == null || sessionId.isBlank()) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
            }

            return new RefreshTokenClaims(
                    Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                    sessionId,
                    jwt.getId()
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }
}
