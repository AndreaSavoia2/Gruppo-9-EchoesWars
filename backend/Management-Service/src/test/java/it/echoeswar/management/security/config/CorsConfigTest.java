package it.echoeswar.management.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void allowsOnlyTheConfiguredOriginWithCredentials() {
        CorsConfiguration configuration = configurationFor(
                new CorsProperties("http://localhost:3000")
        );

        assertEquals(
                "http://localhost:3000",
                configuration.checkOrigin("http://localhost:3000")
        );
        assertNull(configuration.checkOrigin("http://localhost:5173"));
        assertTrue(configuration.getAllowCredentials());
        assertEquals(List.of("GET", "POST"), configuration.getAllowedMethods());
    }

    @Test
    void deniesEveryOriginWhenNoneIsConfigured() {
        CorsConfiguration configuration = configurationFor(new CorsProperties(""));

        assertNull(configuration.checkOrigin("http://localhost:3000"));
    }

    @Test
    void rejectsWildcardOrigins() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CorsProperties("*")
        );
    }

    private CorsConfiguration configurationFor(CorsProperties corsProperties) {
        CorsConfigurationSource source =
                corsConfig.corsConfigurationSource(corsProperties);

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/v1/auth/csrf");

        return source.getCorsConfiguration(request);
    }
}
