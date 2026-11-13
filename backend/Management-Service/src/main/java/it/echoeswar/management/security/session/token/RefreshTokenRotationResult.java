package it.echoeswar.management.security.session.token;

import java.util.Objects;
import java.util.UUID;

/**
 * Contains the owner and replacement token produced by a successful rotation.
 *
 * <p>This is an internal service result and must never be serialized.</p>
 */
public record RefreshTokenRotationResult(
        UUID userId,
        IssuedRefreshToken issuedRefreshToken
) {

    public RefreshTokenRotationResult {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(
                issuedRefreshToken,
                "issuedRefreshToken must not be null"
        );
    }

    @Override
    public String toString() {
        return "RefreshTokenRotationResult[userId=%s, issuedRefreshToken=<redacted>]"
                .formatted(userId);
    }
}
