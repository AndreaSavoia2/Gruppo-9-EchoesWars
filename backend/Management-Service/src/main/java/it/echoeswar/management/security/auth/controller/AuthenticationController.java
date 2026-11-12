package it.echoeswar.management.security.auth.controller;

import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.auth.dto.request.LoginRequest;
import it.echoeswar.management.security.auth.dto.request.RegisterRequest;
import it.echoeswar.management.security.auth.dto.request.ChangePasswordRequest;
import it.echoeswar.management.security.auth.dto.request.SelectAvatarRequest;
import it.echoeswar.management.security.auth.dto.response.AccessTokenResponse;
import it.echoeswar.management.security.auth.dto.response.UserResponse;
import it.echoeswar.management.security.auth.service.AuthenticationResult;
import it.echoeswar.management.security.auth.service.AuthenticationService;
import it.echoeswar.management.security.session.cookie.RefreshTokenCookieFactory;
import it.echoeswar.management.security.session.dto.response.ActiveSessionResponse;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.echoeswar.management.security.jwt.authentication.JwtSessionAuthenticationToken;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final AuthenticationSessionService authenticationSessionService;

    @GetMapping("/sessions")
    public ResponseEntity<ResponseApi<List<ActiveSessionResponse>>> getActiveSessions(
            @AuthenticationPrincipal CustomUserDetails user,
            Authentication authentication
    ) {
        JwtSessionAuthenticationToken sessionAuthentication =
                extractSessionAuthentication(authentication);

        List<ActiveSessionResponse> activeSessions =
                authenticationSessionService.getActiveSessions(
                        user.getId(),
                        sessionAuthentication.getSessionId()
                );

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                activeSessions,
                "Active sessions retrieved successfully."
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseApi<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse userResponse = userService.register(request);

        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                userResponse,
                "User registered successfully."
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseApi<AccessTokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            @RequestHeader(
                    value = HttpHeaders.USER_AGENT,
                    required = false
            ) String userAgent
    ) {
        AuthenticationResult authenticationResult =
                authenticationService.login(
                        request,
                        servletRequest.getRemoteAddr(),
                        userAgent
                );

        return buildAuthenticationResponse(
                authenticationResult,
                "Login successful."
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseApi<AccessTokenResponse>> refresh(
            @CookieValue(
                    name = "${security.refresh-token.cookie.name}",
                    required = false
            ) String rawRefreshToken
    ) {
        AuthenticationResult authenticationResult =
                authenticationService.refresh(rawRefreshToken);

        return buildAuthenticationResponse(
                authenticationResult,
                "Access token refreshed successfully."
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseApi<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authenticationService.changePassword(user.getId(), request);

        return buildLogoutResponse(
                "Password changed successfully. Please sign in again."
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails user,
            Authentication authentication
    ) {
        JwtSessionAuthenticationToken sessionAuthentication =
                extractSessionAuthentication(authentication);

        authenticationService.logout(
                user.getId(),
                sessionAuthentication.getSessionId()
        );

        return buildLogoutResponse("Logged out successfully.");
    }

    private JwtSessionAuthenticationToken extractSessionAuthentication(
            Authentication authentication
    ) {
        if (authentication instanceof JwtSessionAuthenticationToken
                sessionAuthentication) {

            return sessionAuthentication;
        }

        throw new IllegalStateException(
                "Expected a JWT session authentication token."
        );
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ResponseApi<Void>> logoutAll(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        authenticationService.logoutAll(user.getId());

        return buildLogoutResponse(
                "Logged out from all devices successfully."
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseApi<UserResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UserResponse userResponse = userService.getUserById(user.getId());

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                userResponse,
                "Current user retrieved successfully."
        );
    }

    @PutMapping("/me/selected-avatar")
    public ResponseEntity<ResponseApi<UserResponse>> selectAvatar(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody SelectAvatarRequest request
    ) {
        UserResponse userResponse = userService.selectAvatar(
                user.getId(),
                request.avatarId()
        );

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                userResponse,
                "Selected avatar updated successfully."
        );
    }

    private ResponseEntity<ResponseApi<Void>> buildLogoutResponse(
            String message
    ) {
        ResponseCookie deletionCookie =
                refreshTokenCookieFactory.createDeletionCookie();

        ResponseApi<Void> response = ResponseApi.<Void>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(HttpStatus.OK)
                .message(message)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deletionCookie.toString())
                .body(response);
    }

    private ResponseEntity<ResponseApi<AccessTokenResponse>> buildAuthenticationResponse(
            AuthenticationResult authenticationResult,
            String message
    ) {

        AccessTokenResponse accessTokenResponse =
                new AccessTokenResponse(
                        authenticationResult.issuedAccessToken()
                                .accessToken(),
                        "Bearer",
                        authenticationResult.issuedAccessToken()
                                .expiresAt()
                );

        ResponseCookie refreshTokenCookie =
                refreshTokenCookieFactory.createIssuedCookie(
                        authenticationResult.issuedRefreshToken()
                );

        ResponseApi<AccessTokenResponse> response =
                ResponseApi.<AccessTokenResponse>builder()
                        .timestamp(LocalDateTime.now())
                        .httpStatus(HttpStatus.OK)
                        .message(message)
                        .data(accessTokenResponse)
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(response);
    }
}
