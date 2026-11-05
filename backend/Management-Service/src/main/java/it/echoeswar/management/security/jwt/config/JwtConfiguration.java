package it.echoeswar.management.security.jwt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Activates JWT-specific configuration properties.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {
}
