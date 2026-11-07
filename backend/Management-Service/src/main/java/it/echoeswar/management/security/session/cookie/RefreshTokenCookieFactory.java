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