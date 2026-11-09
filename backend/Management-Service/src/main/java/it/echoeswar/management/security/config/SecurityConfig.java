package it.echoeswar.management.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import it.echoeswar.management.security.handler.RestAuthenticationEntryPoint;
import it.echoeswar.management.security.handler.RestAccessDeniedHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import it.echoeswar.management.security.jwt.converter.CustomJwtAuthenticationConverter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import it.echoeswar.management.security.session.config.RefreshTokenCookieProperties;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Configures the HTTP security pipeline for the management REST API.
 *
 * <p>It defines public routes, REST-specific security error responses, password verification,
 * and the stateless request policy.</p>
 *
 * <p>The API does not use form login, HTTP Basic authentication, or server-side HTTP sessions.</p>
 */
@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Writes the standard JSON response for unauthenticated access attempts.
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    // Writes the standard JSON response for authenticated users without sufficient permissions.
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    // Converts validated JWTs into the application-specific authenticated principal.
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    private final RefreshTokenCookieProperties refreshTokenCookieProperties;

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                .path(refreshTokenCookieProperties.path())
                .secure(Boolean.TRUE.equals(
                        refreshTokenCookieProperties.secure()
                ))
                .sameSite(refreshTokenCookieProperties.sameSite())
        );

        return csrfTokenRepository;
    }

    /**
     * Provides BCrypt hashing for password storage and credential verification.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // TODO: Calibrate the BCrypt cost factor against production hardware before deployment.
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Creates the provider that authenticates email and password credentials against application data.
     *
     * <p>The provider delegates user lookup to {@code CustomUserDetailsService} and password
     * comparison to BCrypt.</p>
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        // Retrieves the user snapshot and account state using the submitted email.
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(customUserDetailsService);

        // Compares the submitted password with the stored BCrypt password hash.
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    /**
     * Exposes the application authentication manager for credential-based login operations.
     *
     * <p>Spring Security builds this manager using the registered authentication provider.</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Builds the filter chain applied to every API request.
     *
     * <p>Authentication endpoints are explicitly public. Every other endpoint is protected by default.</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository) throws Exception {
        return http
                // TODO: Replace this with cookie-based CSRF protection before enabling refresh-token cookies.
                //.csrf(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf
                        .spa()
                        .csrfTokenRepository(csrfTokenRepository)
                        .requireCsrfProtectionMatcher(
                                PathPatternRequestMatcher.pathPattern(
                                        HttpMethod.POST,
                                        "/api/v1/auth/refresh"
                                )
                        )
                )
                // Do not persist authentication in an HTTP session.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Prevent Spring Security from saving the original request in the HTTP session.
                .requestCache(requestCache -> requestCache.disable())
                // Disable browser-oriented defaults; authentication is handled by explicit REST endpoints.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Disable the default logout endpoint; a REST logout endpoint will be implemented later.
                .logout(AbstractHttpConfigurer::disable)
                // These handlers run for security failures before any controller is invoked.
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                // Validates Bearer JWTs and exposes CustomUserDetails as the authenticated principal.
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(customJwtAuthenticationConverter)
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/csrf"
                        ).permitAll()
                        // API documentation is publicly readable.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // These operations establish or renew credentials and cannot require an access token.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh"
                        ).permitAll()
                        // Secure every new endpoint by default.
                        .anyRequest().authenticated()
                )
                .build();
    }
}
