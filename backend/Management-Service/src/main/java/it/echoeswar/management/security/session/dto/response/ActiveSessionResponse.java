package it.echoeswar.management.security.session.dto.response;

import it.echoeswar.management.security.session.entity.AuthenticationSession;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ActiveSessionResponse(
        UUID sessionId,
        Instant createdAt,
        Instant lastUsedAt,
        Instant expiresAt,
        String ipAddress,
        String userAgent,
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