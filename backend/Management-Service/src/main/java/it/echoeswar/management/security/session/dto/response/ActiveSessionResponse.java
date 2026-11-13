package it.echoeswar.management.security.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.security.session.entity.AuthenticationSession;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Schema(
        name = "ActiveSessionResponse",
        description = "Metadata for one active authentication session of the current user."
)
public record ActiveSessionResponse(
        @Schema(
                description = "Unique identifier of the authentication session.",
                example = "b3b07384-d9a7-4f4f-8d33-5e0fcb1d53d3",
                format = "uuid",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID sessionId,

        @Schema(
                description = "UTC instant at which the session was created.",
                example = "2026-11-13T10:15:30Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Instant createdAt,

        @Schema(
                description = "UTC instant when the session was last used.",
                example = "2026-11-13T10:30:00Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Instant lastUsedAt,

        @Schema(
                description = "UTC instant at which the session expires.",
                example = "2026-11-20T10:15:30Z",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Instant expiresAt,

        @Schema(
                description = "Client IP address captured when the session was created, if available.",
                example = "203.0.113.42",
                maxLength = 45,
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String ipAddress,

        @Schema(
                description = "Client User-Agent captured when the session was created, if available.",
                example = "Mozilla/5.0",
                maxLength = 512,
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String userAgent,

        @Schema(
                description = "Whether this is the session used for the current request.",
                example = "true",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean currentSession
) {

    public static ActiveSessionResponse from(
            AuthenticationSession authenticationSession,
            UUID currentSessionId
    ) {
        Objects.requireNonNull(
                authenticationSession,
                "authenticationSession must not be null"
        );
        Objects.requireNonNull(
                currentSessionId,
                "currentSessionId must not be null"
        );

        return new ActiveSessionResponse(
                authenticationSession.getId(),
                authenticationSession.getCreatedAt(),
                authenticationSession.getLastUsedAt(),
                authenticationSession.getExpiresAt(),
                authenticationSession.getIpAddress(),
                authenticationSession.getUserAgent(),
                authenticationSession.getId().equals(currentSessionId)
        );
    }
}
