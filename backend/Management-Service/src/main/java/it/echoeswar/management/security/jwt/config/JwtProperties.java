package it.echoeswar.management.security.jwt.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Contains configuration required to issue and validate RS256 access tokens.
 *
 * <p>The key pair is stored in an external PKCS#12 key store and must never be committed.</p>
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String audience,
        @NotNull Duration accessTokenTtl,
        @NotNull Resource keyStoreLocation,
        @NotBlank String keyStorePassword,
        @NotBlank String keyAlias
) {

    @Override
    public String toString() {
        return "JwtProperties[issuer=%s, audience=%s, accessTokenTtl=%s, "
                + "keyStoreLocation=%s, keyStorePassword=<redacted>, keyAlias=%s]"
                .formatted(issuer, audience, accessTokenTtl, keyStoreLocation, keyAlias);
    }
}
