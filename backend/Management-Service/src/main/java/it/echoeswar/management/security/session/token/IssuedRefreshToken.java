package it.echoeswar.management.security.session.token;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Contains the data needed to deliver a newly issued refresh token.
 *
 * <p>The raw token is transient and must never be persisted or logged.</p>
 */
public record IssuedRefreshToken(
        UUID sessionId,
        String rawToken,
        Instant expiresAt
) {

    public IssuedRefreshToken {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(rawToken, "rawToken must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    @Override
    public String toString() {
        return "IssuedRefreshToken[sessionId=%s, rawToken=<redacted>, expiresAt=%s]"
                .formatted(sessionId, expiresAt);
    }
}
