package it.echoeswar.management.security.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Objects;

@Schema(
        name = "AccessTokenResponse",
        description = "Short-lived bearer access token issued after authentication or refresh."
)
public record AccessTokenResponse(
        @Schema(
                description = "JWT access token. Send it in the Authorization header using the Bearer scheme.",
                example = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbmRyZWEifQ.signature",
                format = "jwt",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String accessToken,

        @Schema(
                description = "Authentication scheme for the access token.",
                example = "Bearer",
                allowableValues = {"Bearer"},
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String tokenType,

        @Schema(
                description = "UTC instant at which the access token expires.",
                example = "2026-11-13T12:30:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
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
