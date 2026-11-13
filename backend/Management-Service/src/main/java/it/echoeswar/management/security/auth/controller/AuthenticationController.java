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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Authentication",
        description = "Registration, sign-in, session, and account operations"
)
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final AuthenticationSessionService authenticationSessionService;

    @Operation(
            summary = "List active sessions",
            description = "Returns active login sessions for the authenticated user. "
                    + "The currentSession field identifies the session represented by the bearer token."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active sessions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @GetMapping("/sessions")
    public ResponseEntity<ResponseApi<List<ActiveSessionResponse>>> getActiveSessions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(hidden = true)
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

    @Operation(
            summary = "Register a user",
            description = "Creates an active user account after validating the submitted credentials."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data or password policy violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email address is already in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
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

    @Operation(
            summary = "Sign in",
            description = "Authenticates email and password credentials, returns a short-lived access token, "
                    + "and sets an HTTP-only refresh-token cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication completed successfully",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "HTTP-only refresh-token cookie."
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid login request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseApi<AccessTokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true)
            HttpServletRequest servletRequest,
            @Parameter(
                    in = ParameterIn.HEADER,
                    description = "Optional client user-agent stored with the authentication session.",
                    example = "Mozilla/5.0"
            )
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

    @Operation(
            summary = "Refresh an access token",
            description = "Consumes the current refresh token once and issues a replacement refresh-token cookie "
                    + "together with a new access token. The request is protected by the CSRF token issued by "
                    + "the CSRF endpoint."
    )
    @Parameters({
            @Parameter(
                    name = "ECHOESWAR_REFRESH_TOKEN",
                    in = ParameterIn.COOKIE,
                    description = "Refresh token cookie. Its configured name can differ by environment.",
                    required = false
            ),
            @Parameter(
                    name = "XSRF-TOKEN",
                    in = ParameterIn.COOKIE,
                    description = "CSRF cookie issued by the CSRF endpoint.",
                    required = true
            ),
            @Parameter(
                    name = "X-XSRF-TOKEN",
                    in = ParameterIn.HEADER,
                    description = "CSRF token copied from the XSRF-TOKEN cookie.",
                    required = true
            )
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access token refreshed successfully",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Replacement HTTP-only refresh-token cookie."
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token is missing, invalid, expired, consumed, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "CSRF token is missing or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ResponseApi<AccessTokenResponse>> refresh(
            @Parameter(hidden = true)
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

    @Operation(
            summary = "Change the current password",
            description = "Replaces the authenticated user's password, revokes every active session, "
                    + "and clears the refresh-token cookie."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed and active sessions revoked",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Deletion refresh-token cookie."
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid password data, password policy violation, or unchanged password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token or current password is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PostMapping("/change-password")
    public ResponseEntity<ResponseApi<Void>> changePassword(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authenticationService.changePassword(user.getId(), request);

        return buildLogoutResponse(
                "Password changed successfully. Please sign in again."
        );
    }

    @Operation(
            summary = "Sign out from the current session",
            description = "Revokes only the session represented by the bearer token and clears the refresh-token cookie."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current session revoked successfully",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Deletion refresh-token cookie."
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<Void>> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(hidden = true)
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

    @Operation(
            summary = "Sign out from all sessions",
            description = "Revokes every active session for the authenticated user and clears the refresh-token cookie."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "All active sessions revoked successfully",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "Deletion refresh-token cookie."
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PostMapping("/logout-all")
    public ResponseEntity<ResponseApi<Void>> logoutAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        authenticationService.logoutAll(user.getId());

        return buildLogoutResponse(
                "Logged out from all devices successfully."
        );
    }

    @Operation(
            summary = "Retrieve the current user",
            description = "Returns the account details associated with the bearer token."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user is not allowed to access the account",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Authenticated user no longer exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseApi<UserResponse>> getMe(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UserResponse userResponse = userService.getUserById(user.getId());

        return ResponseApi.buildResponse(
                HttpStatus.OK,
                userResponse,
                "Current user retrieved successfully."
        );
    }

    @Operation(
            summary = "Select an unlocked avatar",
            description = "Sets the authenticated user's selected avatar. The avatar must already be unlocked by that user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Selected avatar updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Avatar ID is invalid or the avatar is not unlocked by the user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, expired, or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user is not allowed to update the selected avatar",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Authenticated user no longer exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseApi.class)
                    )
            )
    })
    @PutMapping("/me/selected-avatar")
    public ResponseEntity<ResponseApi<UserResponse>> selectAvatar(
            @Parameter(hidden = true)
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
