package it.echoeswar.management.security.session.cookie;

import it.echoeswar.management.security.session.config.RefreshTokenCookieProperties;
import it.echoeswar.management.security.session.token.IssuedRefreshToken;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Builds secure HTTP cookies used to transport refresh tokens.
 */
@Component
public class RefreshTokenCookieFactory {

    private final RefreshTokenCookieProperties cookieProperties;

    public RefreshTokenCookieFactory(
            RefreshTokenCookieProperties cookieProperties
    ) {
        this.cookieProperties = cookieProperties;
    }

    /**
     * Creates the HTTP-only cookie that transports a newly issued raw refresh token.
     *
     * <p>The cookie lifetime is derived from the token's absolute expiration so the browser does
     * not retain it longer than the corresponding server-side session.</p>
     */
    public ResponseCookie createIssuedCookie(
            IssuedRefreshToken issuedRefreshToken
    ) {
        Objects.requireNonNull(
                issuedRefreshToken,
                "issuedRefreshToken must not be null"
        );

        Duration maxAge = Duration.between(
                Instant.now(),
                issuedRefreshToken.expiresAt()
        );

        if (maxAge.isZero() || maxAge.isNegative()) {
            throw new IllegalArgumentException(
                    "Refresh token expiration must be in the future."
            );
        }

        return cookieBuilder(issuedRefreshToken.rawToken())
                .maxAge(maxAge)
                .build();
    }

    /**
     * Creates a zero-age cookie with the configured scope to remove a stored refresh token.
     *
     * <p>Using the same name, path, secure flag, and SameSite policy as an issued cookie is
     * required for browsers to replace it.</p>
     */
    public ResponseCookie createDeletionCookie() {
        return cookieBuilder("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder cookieBuilder(
            String value
    ) {
        return ResponseCookie.from(cookieProperties.name(), value)
                .httpOnly(true)
                .secure(Boolean.TRUE.equals(cookieProperties.secure()))
                .path(cookieProperties.path())
                .sameSite(cookieProperties.sameSite());
    }
}
