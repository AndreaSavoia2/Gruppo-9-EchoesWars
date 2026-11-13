package it.echoeswar.management.security.session.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Contains configuration used by refresh-token management.
 */
@Validated
@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenProperties(
        @NotNull Duration ttl
) {
}
