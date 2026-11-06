package it.echoeswar.management.security.session.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers configuration properties used by authentication sessions.
 */
@Configuration
@EnableConfigurationProperties(RefreshTokenProperties.class)
public class SessionConfiguration {
}
