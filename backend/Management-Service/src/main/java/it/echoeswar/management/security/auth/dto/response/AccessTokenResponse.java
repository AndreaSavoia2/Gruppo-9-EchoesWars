package it.echoeswar.management.security.auth.dto.response;

import java.time.Instant;
import java.util.Objects;

public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt
) {

    public AccessTokenResponse {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(tokenType, "tokenType must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");

        if (accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "accessToken must not be blank."
            );
        }

        if (tokenType.isBlank()) {
            throw new IllegalArgumentException(
                    "tokenType must not be blank."
            );
        }
    }

    @Override
    public String toString() {
        return "AccessTokenResponse[accessToken=[REDACTED], tokenType="
                + tokenType
                + ", expiresAt="
                + expiresAt
                + "]";
    }
}
