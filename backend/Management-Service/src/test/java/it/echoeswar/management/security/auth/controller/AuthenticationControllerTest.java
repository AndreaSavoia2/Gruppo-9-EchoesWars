package it.echoeswar.management.security.auth.controller;

import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.auth.dto.request.ChangePasswordRequest;
import it.echoeswar.management.security.auth.service.AuthenticationService;
import it.echoeswar.management.security.session.cookie.RefreshTokenCookieFactory;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import it.echoeswar.management.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationControllerTest {

    @Test
    void changePasswordRevokesTheCurrentClientRefreshCookie() {
        UserService userService = mock(UserService.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        RefreshTokenCookieFactory refreshTokenCookieFactory =
                mock(RefreshTokenCookieFactory.class);
        AuthenticationSessionService authenticationSessionService =
                mock(AuthenticationSessionService.class);

        AuthenticationController controller = new AuthenticationController(
                userService,
                authenticationService,
                refreshTokenCookieFactory,
                authenticationSessionService
        );

        UUID userId = UUID.randomUUID();
        CustomUserDetails user = CustomUserDetails.forJwtAuthentication(
                User.builder()
                        .id(userId)
                        .email("user@example.com")
                        .accountStatus(AccountStatus.ACTIVE)
                        .role(new Role(RoleName.ROLE_USER))
                        .build()
        );
        ChangePasswordRequest request = new ChangePasswordRequest(
                "current-password",
                "new-secure-password"
        );
        ResponseCookie deletionCookie = ResponseCookie.from(
                        "ECHOESWAR_REFRESH_TOKEN",
                        ""
                )
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();

        when(refreshTokenCookieFactory.createDeletionCookie())
                .thenReturn(deletionCookie);

        ResponseEntity<ResponseApi<Void>> response = controller.changePassword(
                user,
                request
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                deletionCookie.toString(),
                response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)
        );
        assertEquals(
                "Password changed successfully. Please sign in again.",
                response.getBody().getMessage()
        );
        verify(authenticationService).changePassword(userId, request);
    }
}
