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
 * <p>Key resources must be supplied externally and must never be stored in the repository.</p>
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String audience,
        @NotNull Duration accessTokenTtl,
        @NotNull Resource privateKeyLocation,
        @NotNull Resource publicKeyLocation
) {
}
