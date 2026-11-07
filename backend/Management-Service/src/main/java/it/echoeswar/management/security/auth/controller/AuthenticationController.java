package it.echoeswar.management.security.auth.controller;

import it.echoeswar.management.common.api.ResponseApi;
import it.echoeswar.management.security.auth.dto.request.LoginRequest;
import it.echoeswar.management.security.auth.dto.request.RegisterRequest;
import it.echoeswar.management.security.auth.dto.response.AccessTokenResponse;
import it.echoeswar.management.security.auth.dto.response.UserResponse;
import it.echoeswar.management.security.auth.service.AuthenticationResult;
import it.echoeswar.management.security.auth.service.AuthenticationService;
import it.echoeswar.management.security.session.cookie.RefreshTokenCookieFactory;
import it.echoeswar.management.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    public AuthenticationController(
            UserService userService,
            AuthenticationService authenticationService,
            RefreshTokenCookieFactory refreshTokenCookieFactory
    ) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.refreshTokenCookieFactory = refreshTokenCookieFactory;
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
                        .message("Login successful.")
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