package it.echoeswar.management.security.auth.service;

import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.security.auth.dto.request.ChangePasswordRequest;
import it.echoeswar.management.security.auth.dto.request.LoginRequest;
import it.echoeswar.management.security.auth.password.PasswordPolicy;
import it.echoeswar.management.security.jwt.service.JwtTokenService;
import it.echoeswar.management.security.jwt.token.IssuedAccessToken;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.session.token.IssuedRefreshToken;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.entity.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Coordinates credential authentication with the stateful session and token lifecycle.
 *
 * <p>This service deliberately exposes the same authentication failure to callers for
 * invalid credentials and unavailable accounts, so account state is not disclosed.</p>
 */
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
    private final PasswordEncoder passwordEncoder;

    /**
     * Revokes one active session owned by the user and all refresh tokens issued for it.
     *
     * <p>The operation is intentionally idempotent: revoking an already inactive session has
     * no effect.</p>
     */
    public void logout(
            UUID userId,
            UUID sessionId
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        authenticationSessionService.revokeSession(
                userId,
                sessionId,
                SessionRevocationReason.LOGOUT
        );
    }

    /**
     * Revokes every active session and refresh-token family belonging to the user.
     *
     * <p>The operation is intentionally idempotent when the user has no active sessions.</p>
     */
    public void logoutAll(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        authenticationSessionService.revokeAllSessions(
                userId,
                SessionRevocationReason.LOGOUT_ALL
        );
    }

    /**
     * Authenticates credentials and creates a new persistent session.
     *
     * <p>The user row is locked before authentication so the account-state check and the
     * last-login update are serialized with concurrent account changes. On success, the result
     * contains a short-lived access token and a raw refresh token; only the refresh-token hash
     * is persisted by the session service.</p>
     */
    @Transactional
    public AuthenticationResult login(
            LoginRequest request,
            String ipAddress,
            String userAgent
    ) {
        Objects.requireNonNull(request, "request must not be null");

        String normalizedEmail = FormatterString.normalizeEmail(request.email());

        validateLoginPassword(request.password());
        lockUserForLogin(normalizedEmail);

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

    /**
     * Replaces the authenticated user's password and invalidates every existing login session.
     *
     * <p>No replacement session is issued: the user must authenticate again with the new
     * password. The transaction makes the password update and global revocation atomic.</p>
     */
    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordRequest request
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(request, "request must not be null");

        validateCurrentPassword(request.currentPassword());
        passwordPolicy.validateForRegistration(request.newPassword());

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(this::invalidCredentials);

        if (!user.canAuthenticate(false)
                || !passwordEncoder.matches(
                        request.currentPassword(),
                        user.getPassword()
                )) {
            throw invalidCredentials();
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must differ from the current password."
            );
        }

        Instant now = Instant.now();

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(now);

        authenticationSessionService.revokeAllSessions(
                userId,
                SessionRevocationReason.PASSWORD_CHANGE
        );
    }

    private void lockUserForLogin(String normalizedEmail) {
        userRepository.findByEmailIgnoreCaseForUpdate(normalizedEmail)
                .orElseThrow(this::invalidCredentials);
    }

    private void validateLoginPassword(String rawPassword) {
        try {
            passwordPolicy.validateForAuthentication(rawPassword);
        } catch (IllegalArgumentException exception) {
            throw invalidCredentials();
        }
    }

    private void validateCurrentPassword(String rawPassword) {
        try {
            passwordPolicy.validateForAuthentication(rawPassword);
        } catch (IllegalArgumentException exception) {
            throw invalidCredentials();
        }
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

    /**
     * Rotates the refresh token and issues a new access-token/refresh-token pair.
     *
     * <p>Rotation consumes the presented token before issuing its replacement. The current
     * account state is then revalidated, and an unusable account causes the refreshed session to
     * be revoked. {@link InvalidRefreshTokenException} does not roll back the transaction so a
     * revocation caused by token reuse is retained.</p>
     */
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
