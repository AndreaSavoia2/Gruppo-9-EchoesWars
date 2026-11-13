package it.echoeswar.management.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Contains the comma-separated list of browser origins allowed to call the API.
 *
 * <p>An empty value denies cross-origin browser requests. Origins must be explicit because
 * the API accepts credentialed requests for refresh-token cookies.</p>
 */
@ConfigurationProperties(prefix = "security.cors")
public record CorsProperties(String allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? "" : allowedOrigins;

        if (parseOrigins(allowedOrigins).stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalArgumentException(
                    "CORS origins must be explicit; wildcard origins are not allowed."
            );
        }
    }

    public List<String> allowedOriginList() {
        return parseOrigins(allowedOrigins);
    }

    private static List<String> parseOrigins(String origins) {
        return Arrays.stream(origins.split(","))
                .map(String::strip)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
