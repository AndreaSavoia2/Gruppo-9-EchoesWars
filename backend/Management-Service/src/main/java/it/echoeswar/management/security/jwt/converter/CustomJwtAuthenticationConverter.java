package it.echoeswar.management.security.jwt.converter;

import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts a validated JWT into an authenticated application principal.
 */
@Component
public class CustomJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String INVALID_ACCESS_TOKEN_MESSAGE = "Invalid access token.";

    private final CustomUserDetailsService customUserDetailsService;

    public CustomJwtAuthenticationConverter(
            CustomUserDetailsService customUserDetailsService) {

        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId = parseUserId(jwt.getSubject());

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

        return new PreAuthenticatedAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private UUID parseUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new InvalidBearerTokenException(INVALID_ACCESS_TOKEN_MESSAGE);
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBearerTokenException(
                    INVALID_ACCESS_TOKEN_MESSAGE,
                    exception
            );
        }
    }
}