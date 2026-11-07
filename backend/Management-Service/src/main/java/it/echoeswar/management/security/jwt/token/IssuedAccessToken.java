package it.echoeswar.management.security.jwt.token;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a signed access token that is ready to be delivered to a client.
 */
public record IssuedAccessToken(
        String accessToken,
        Instant expiresAt
) {

    public IssuedAccessToken {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");

        if (accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "accessToken must not be blank."
            );
        }
    }

    @Override
    public String toString() {
        return "IssuedAccessToken[accessToken=[REDACTED], expiresAt="
                + expiresAt
                + "]";
    }
}
