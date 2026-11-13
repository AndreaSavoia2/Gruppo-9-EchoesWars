package it.echoeswar.management.security.jwt.converter;

import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import it.echoeswar.management.security.jwt.authentication.JwtSessionAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts a validated JWT into an authenticated application principal.
 */
@Component
public class CustomJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String INVALID_ACCESS_TOKEN_MESSAGE = "Invalid access token.";
    private static final String SESSION_ID_CLAIM = "sid";

    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationSessionService authenticationSessionService;

    public CustomJwtAuthenticationConverter(
            CustomUserDetailsService customUserDetailsService,
            AuthenticationSessionService authenticationSessionService
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationSessionService = authenticationSessionService;
    }

    /**
     * Builds an authenticated principal only when the JWT still represents an active account and session.
     *
     * <p>Signature and standard JWT claims have already been verified by the resource server. This
     * additional lookup prevents a token from remaining valid after an account is disabled or its
     * server-side session is revoked.</p>
     *
     * @param jwt validated JWT supplied by the resource server
     * @return application-specific authentication containing the user and session ID
     * @throws InvalidBearerTokenException if the subject or session claim is invalid, the account
     *                                     cannot authenticate, or the session is inactive
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId = parseUserId(jwt.getSubject());
        UUID sessionId = parseSessionId(jwt);

        CustomUserDetails userDetails;
        try {
            userDetails = customUserDetailsService.loadUserById(userId);
        } catch (UsernameNotFoundException exception) {
            throw new InvalidBearerTokenException(
                    INVALID_ACCESS_TOKEN_MESSAGE,
                    exception
            );
        }

        if (!userDetails.isEnabled()
                || !userDetails.isAccountNonLocked()
                || !userDetails.isAccountNonExpired()
                || !userDetails.isCredentialsNonExpired()) {

            throw new InvalidBearerTokenException(INVALID_ACCESS_TOKEN_MESSAGE);
        }

        if (!authenticationSessionService.isSessionActive(userId, sessionId)) {
            throw new InvalidBearerTokenException(INVALID_ACCESS_TOKEN_MESSAGE);
        }

        return new JwtSessionAuthenticationToken(
                userDetails,
                sessionId
        );
    }

    private UUID parseUserId(String subject) {
        return parseUuid(subject);
    }

    private UUID parseSessionId(Jwt jwt) {
        Object sessionIdClaim = jwt.getClaim(SESSION_ID_CLAIM);

        if (!(sessionIdClaim instanceof String sessionIdValue)) {
            throw new InvalidBearerTokenException(INVALID_ACCESS_TOKEN_MESSAGE);
        }

        return parseUuid(sessionIdValue);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidBearerTokenException(INVALID_ACCESS_TOKEN_MESSAGE);
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBearerTokenException(
                    INVALID_ACCESS_TOKEN_MESSAGE,
                    exception
            );
        }
    }
}
