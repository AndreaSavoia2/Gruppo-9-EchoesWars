package it.echoeswar.management.security.jwt.authentication;

import it.echoeswar.management.security.userdetails.CustomUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an authenticated JWT request associated with one persistent session.
 */
public final class JwtSessionAuthenticationToken
        extends AbstractAuthenticationToken {

    private final CustomUserDetails principal;
    private final UUID sessionId;

    public JwtSessionAuthenticationToken(
            CustomUserDetails principal,
            UUID sessionId
    ) {
        super(Objects.requireNonNull(
                principal,
                "principal must not be null"
        ).getAuthorities());

        this.principal = principal;
        this.sessionId = Objects.requireNonNull(
                sessionId,
                "sessionId must not be null"
        );

        super.setAuthenticated(true);
    }

    @Override
    public CustomUserDetails getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Use the constructor to create an authenticated token."
            );
        }

        super.setAuthenticated(false);
    }
}