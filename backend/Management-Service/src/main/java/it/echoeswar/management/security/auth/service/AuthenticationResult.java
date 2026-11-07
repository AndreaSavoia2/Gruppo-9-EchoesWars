package it.echoeswar.management.security.auth.service;

import it.echoeswar.management.security.jwt.token.IssuedAccessToken;
import it.echoeswar.management.security.session.token.IssuedRefreshToken;

import java.util.Objects;

/**
 * Holds credentials produced by a successful authentication flow.
 *
 * <p>This is an internal application result and must never be serialized directly.</p>
 */
public record AuthenticationResult(
        IssuedAccessToken issuedAccessToken,
        IssuedRefreshToken issuedRefreshToken
) {

    public AuthenticationResult {
        Objects.requireNonNull(
                issuedAccessToken,
                "issuedAccessToken must not be null"
        );
        Objects.requireNonNull(
                issuedRefreshToken,
                "issuedRefreshToken must not be null"
        );
    }

    @Override
    public String toString() {
        return "AuthenticationResult[issuedAccessToken=<redacted>, "
                + "issuedRefreshToken=<redacted>]";
    }
}
