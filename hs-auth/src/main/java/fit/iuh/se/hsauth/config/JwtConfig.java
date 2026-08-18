package fit.iuh.se.hsauth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class JwtConfig {

    @Value("${security.jwt.key-id:healthsense-auth-key}")
    private String keyId;

    @Bean
    public RSAPrivateKey jwtPrivateKey(
            @Value("${security.jwt.private-key-location:classpath:keys/private.pem}") Resource privateKeyResource,
            ResourceLoader resourceLoader) throws Exception {
        log.info("[JwtConfig] Loading Private Key. Primary location description: '{}', exists: {}",
                privateKeyResource.getDescription(), privateKeyResource.exists());

        if (privateKeyResource.exists()) {
            try {
                String pem = privateKeyResource.getContentAsString(StandardCharsets.UTF_8);
                byte[] keyBytes = parsePem(pem);
                RSAPrivateKey key = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
                log.info("[JwtConfig] Successfully loaded RSAPrivateKey from primary location: {}", privateKeyResource.getDescription());
                return key;
            } catch (Exception e) {
                log.error("[JwtConfig] Failed to load/parse RSAPrivateKey from primary location ({}): {}. Falling back to classpath:keys/private.pem",
                        privateKeyResource.getDescription(), e.getMessage(), e);
            }
        } else {
            log.warn("[JwtConfig] Primary Private Key resource ({}) does not exist. Falling back to classpath:keys/private.pem",
                    privateKeyResource.getDescription());
        }

        Resource fallbackResource = resourceLoader.getResource("classpath:keys/private.pem");
        log.info("[JwtConfig] Loading fallback Private Key from classpath:keys/private.pem. Exists: {}", fallbackResource.exists());
        String pem = fallbackResource.getContentAsString(StandardCharsets.UTF_8);
        byte[] keyBytes = parsePem(pem);
        RSAPrivateKey key = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        log.info("[JwtConfig] Successfully loaded RSAPrivateKey from fallback classpath:keys/private.pem");
        return key;
    }

    @Bean
    public RSAPublicKey jwtPublicKey(
            @Value("${security.jwt.public-key-location:classpath:keys/public.pem}") Resource publicKeyResource,
            ResourceLoader resourceLoader) throws Exception {
        log.info("[JwtConfig] Loading Public Key. Primary location description: '{}', exists: {}",
                publicKeyResource.getDescription(), publicKeyResource.exists());

        if (publicKeyResource.exists()) {
            try {
                String pem = publicKeyResource.getContentAsString(StandardCharsets.UTF_8);
                byte[] keyBytes = parsePem(pem);
                RSAPublicKey key = (RSAPublicKey) KeyFactory.getInstance("RSA")
                        .generatePublic(new X509EncodedKeySpec(keyBytes));
                log.info("[JwtConfig] Successfully loaded RSAPublicKey from primary location: {}", publicKeyResource.getDescription());
                return key;
            } catch (Exception e) {
                log.error("[JwtConfig] Failed to load/parse RSAPublicKey from primary location ({}): {}. Falling back to classpath:keys/public.pem",
                        publicKeyResource.getDescription(), e.getMessage(), e);
            }
        } else {
            log.warn("[JwtConfig] Primary Public Key resource ({}) does not exist. Falling back to classpath:keys/public.pem",
                    publicKeyResource.getDescription());
        }

        Resource fallbackResource = resourceLoader.getResource("classpath:keys/public.pem");
        log.info("[JwtConfig] Loading fallback Public Key from classpath:keys/public.pem. Exists: {}", fallbackResource.exists());
        String pem = fallbackResource.getContentAsString(StandardCharsets.UTF_8);
        byte[] keyBytes = parsePem(pem);
        RSAPublicKey key = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));
        log.info("[JwtConfig] Successfully loaded RSAPublicKey from fallback classpath:keys/public.pem");
        return key;
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        log.info("[JwtConfig] Registering JwtEncoder bean with keyId: {}", keyId);
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        log.info("[JwtConfig] Registering JwtDecoder bean with RSAPublicKey...");
        JwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        log.info("[JwtConfig] Successfully created and registered JwtDecoder bean!");
        return decoder;
    }

    private byte[] parsePem(String pem) {
        if (pem == null) {
            throw new IllegalArgumentException("PEM string cannot be null");
        }
        String normalized = pem
                .replace("\uFEFF", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("[\\r\\n\\s\"]", "");
        return Base64.getDecoder().decode(normalized);
    }
}
