package it.echoeswar.management.security.session.token;

import java.util.Objects;

/**
 * Holds a newly issued raw refresh token and its persistent hash.
 *
 * <p>The raw token is transient and must never be persisted or logged.</p>
 */
public record GeneratedRefreshToken(
        String rawToken,
        String tokenHash
) {

    public GeneratedRefreshToken {
        Objects.requireNonNull(rawToken, "rawToken must not be null");
        Objects.requireNonNull(tokenHash, "tokenHash must not be null");
    }

    @Override
    public String toString() {
        return "GeneratedRefreshToken[rawToken=<redacted>, tokenHash=<redacted>]";
    }
}
