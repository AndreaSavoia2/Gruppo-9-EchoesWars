package it.echoeswar.management.security.auth.service;

import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.security.auth.dto.request.LoginRequest;
import it.echoeswar.management.security.auth.password.PasswordPolicy;
import it.echoeswar.management.security.jwt.service.JwtTokenService;
import it.echoeswar.management.security.jwt.token.IssuedAccessToken;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.session.token.IssuedRefreshToken;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.echoeswar.management.security.session.enumerated.SessionRevocationReason;
import it.echoeswar.management.security.session.exception.InvalidRefreshTokenException;
import it.echoeswar.management.security.session.token.RefreshTokenRotationResult;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password.";
    private static final int MAXIMUM_IP_ADDRESS_LENGTH = 45;
    private static final int MAXIMUM_USER_AGENT_LENGTH = 512;

    private final AuthenticationManager authenticationManager;
    private final PasswordPolicy passwordPolicy;
    private final UserRepository userRepository;
    private final AuthenticationSessionService authenticationSessionService;
    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public AuthenticationResult login(
            LoginRequest request,
            String ipAddress,
            String userAgent
    ) {
        Objects.requireNonNull(request, "request must not be null");

        String normalizedEmail = FormatterString.normalizeEmail(request.email());

        Authentication authentication = authenticate(
                normalizedEmail,
                request.password()
        );

        CustomUserDetails userDetails = extractUserDetails(authentication);

        int updatedUserCount = userRepository.updateLastLoginAtIfAccountMatches(
                userDetails.getId(),
                Instant.now(),
                AccountStatus.ACTIVE
        );

        if (updatedUserCount != 1) {
            throw invalidCredentials();
        }

        IssuedRefreshToken issuedRefreshToken =
                authenticationSessionService.createSession(
                        userDetails.getId(),
                        limitClientMetadata(
                                ipAddress,
                                MAXIMUM_IP_ADDRESS_LENGTH
                        ),
                        limitClientMetadata(
                                userAgent,
                                MAXIMUM_USER_AGENT_LENGTH
                        )
                );

        IssuedAccessToken issuedAccessToken =
                jwtTokenService.generateAccessToken(
                        userDetails,
                        issuedRefreshToken.sessionId()
                );

        return new AuthenticationResult(
                issuedAccessToken,
                issuedRefreshToken
        );
    }

    private CustomUserDetails loadActiveUserForRefresh(
            RefreshTokenRotationResult rotationResult
    ) {
        try {
            CustomUserDetails userDetails =
                    customUserDetailsService.loadUserById(
                            rotationResult.userId()
                    );

            if (!userDetails.isEnabled()
                    || !userDetails.isAccountNonLocked()
                    || !userDetails.isAccountNonExpired()
                    || !userDetails.isCredentialsNonExpired()) {

                revokeRefreshSession(rotationResult);
                throw new InvalidRefreshTokenException();
            }

            return userDetails;
        } catch (UsernameNotFoundException exception) {
            revokeRefreshSession(rotationResult);
            throw new InvalidRefreshTokenException();
        }
    }

    private void revokeRefreshSession(
            RefreshTokenRotationResult rotationResult
    ) {
        authenticationSessionService.revokeSession(
                rotationResult.userId(),
                rotationResult.issuedRefreshToken().sessionId(),
                SessionRevocationReason.ADMIN_ACTION
        );
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthenticationResult refresh(String rawRefreshToken) {
        RefreshTokenRotationResult rotationResult =
                authenticationSessionService.rotateRefreshToken(rawRefreshToken);

        CustomUserDetails userDetails =
                loadActiveUserForRefresh(rotationResult);

        IssuedAccessToken issuedAccessToken =
                jwtTokenService.generateAccessToken(
                        userDetails,
                        rotationResult.issuedRefreshToken().sessionId()
                );

        return new AuthenticationResult(
                issuedAccessToken,
                rotationResult.issuedRefreshToken()
        );
    }

    private Authentication authenticate(String normalizedEmail, String rawPassword) {
        try {
            passwordPolicy.validateForAuthentication(rawPassword);

            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            normalizedEmail,
                            rawPassword
                    )
            );
        } catch (AuthenticationServiceException exception) {
            throw exception;
        } catch (AuthenticationException | IllegalArgumentException exception) {
            throw invalidCredentials();
        }
    }

    private CustomUserDetails extractUserDetails(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new IllegalStateException(
                "Authentication manager returned an unsupported principal."
        );
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
    }

    private String limitClientMetadata(
            String value,
            int maximumLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.length() <= maximumLength
                ? value
                : value.substring(0, maximumLength);
    }
}
