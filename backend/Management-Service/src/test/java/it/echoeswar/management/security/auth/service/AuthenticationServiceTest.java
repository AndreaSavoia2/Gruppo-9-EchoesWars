package it.echoeswar.management.security.auth.service;

import it.echoeswar.management.security.auth.dto.request.ChangePasswordRequest;
import it.echoeswar.management.security.auth.dto.request.LoginRequest;
import it.echoeswar.management.security.auth.password.PasswordPolicy;
import it.echoeswar.management.security.jwt.service.JwtTokenService;
import it.echoeswar.management.security.jwt.token.IssuedAccessToken;
import it.echoeswar.management.security.session.enumerated.SessionRevocationReason;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.session.token.IssuedRefreshToken;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import it.echoeswar.management.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String CURRENT_PASSWORD = "current-password";
    private static final String NEW_PASSWORD = "new-secure-password";
    private static final String CURRENT_PASSWORD_HASH = "current-password-hash";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordPolicy passwordPolicy;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationSessionService authenticationSessionService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void changePasswordUpdatesHashAndRevokesEverySession() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        ChangePasswordRequest request = new ChangePasswordRequest(
                CURRENT_PASSWORD,
                NEW_PASSWORD
        );

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_PASSWORD_HASH))
                .thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, CURRENT_PASSWORD_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("new-password-hash");

        authenticationService.changePassword(userId, request);

        assertEquals("new-password-hash", user.getPassword());
        assertNotNull(user.getPasswordChangedAt());
        verify(passwordPolicy).validateForAuthentication(CURRENT_PASSWORD);
        verify(passwordPolicy).validateForRegistration(NEW_PASSWORD);
        verify(authenticationSessionService).revokeAllSessions(
                userId,
                SessionRevocationReason.PASSWORD_CHANGE
        );
    }

    @Test
    void changePasswordWithWrongCurrentPasswordLeavesSessionsUntouched() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_PASSWORD_HASH))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.changePassword(
                        userId,
                        new ChangePasswordRequest(CURRENT_PASSWORD, NEW_PASSWORD)
                )
        );

        assertEquals(CURRENT_PASSWORD_HASH, user.getPassword());
        verify(authenticationSessionService, never()).revokeAllSessions(
                userId,
                SessionRevocationReason.PASSWORD_CHANGE
        );
        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
    }

    @Test
    void changePasswordRejectsReuseOfTheCurrentPassword() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_PASSWORD_HASH))
                .thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, CURRENT_PASSWORD_HASH))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.changePassword(
                        userId,
                        new ChangePasswordRequest(CURRENT_PASSWORD, NEW_PASSWORD)
                )
        );

        assertEquals(CURRENT_PASSWORD_HASH, user.getPassword());
        verifyNoInteractions(authenticationSessionService);
        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
    }

    @Test
    void loginLocksTheUserBeforeAuthenticatingAndCreatingItsSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = activeUser(userId);
        user.setEmail("user@example.com");
        user.setRole(new Role(RoleName.ROLE_USER));

        CustomUserDetails userDetails =
                CustomUserDetails.forPasswordAuthentication(user);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        when(userRepository.findByEmailIgnoreCaseForUpdate("user@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.updateLastLoginAtIfAccountMatches(
                eq(userId),
                any(Instant.class),
                eq(AccountStatus.ACTIVE)
        )).thenReturn(1);
        when(authenticationSessionService.createSession(
                eq(userId),
                eq("127.0.0.1"),
                eq("test-agent")
        )).thenReturn(new IssuedRefreshToken(
                sessionId,
                "refresh-token",
                Instant.now().plusSeconds(60)
        ));
        when(jwtTokenService.generateAccessToken(userDetails, sessionId))
                .thenReturn(new IssuedAccessToken(
                        "access-token",
                        Instant.now().plusSeconds(60)
                ));

        authenticationService.login(
                new LoginRequest("user@example.com", CURRENT_PASSWORD),
                "127.0.0.1",
                "test-agent"
        );

        InOrder order = inOrder(
                userRepository,
                authenticationManager,
                authenticationSessionService
        );
        order.verify(userRepository)
                .findByEmailIgnoreCaseForUpdate("user@example.com");
        order.verify(authenticationManager).authenticate(any());
        order.verify(userRepository).updateLastLoginAtIfAccountMatches(
                eq(userId),
                any(Instant.class),
                eq(AccountStatus.ACTIVE)
        );
        order.verify(authenticationSessionService).createSession(
                eq(userId),
                eq("127.0.0.1"),
                eq("test-agent")
        );
    }

    private User activeUser(UUID userId) {
        return User.builder()
                .id(userId)
                .password(CURRENT_PASSWORD_HASH)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }
}
