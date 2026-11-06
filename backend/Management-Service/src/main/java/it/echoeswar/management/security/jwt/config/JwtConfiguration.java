package it.echoeswar.management.security.jwt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Configures the RSA key material used to issue and validate JWT access tokens.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    private static final String KEY_STORE_TYPE = "PKCS12";

    /**
     * Loads the external key store once during application startup.
     */
    @Bean
    public KeyStore jwtKeyStore(JwtProperties jwtProperties) {
        char[] password = jwtProperties.keyStorePassword().toCharArray();

        try (InputStream inputStream = jwtProperties.keyStoreLocation().getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            keyStore.load(inputStream, password);

            return keyStore;
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("JWT key store could not be loaded.", exception);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Extracts the RSA private key used to sign access tokens.
     */
    @Bean
    public RSAPrivateKey jwtPrivateKey(
            KeyStore jwtKeyStore,
            JwtProperties jwtProperties
    ) {
        char[] password = jwtProperties.keyStorePassword().toCharArray();

        try {
            Key key = jwtKeyStore.getKey(jwtProperties.keyAlias(), password);

            if (key instanceof RSAPrivateKey privateKey) {
                return privateKey;
            }

            throw new IllegalStateException(
                    "JWT key store does not contain an RSA private key for the configured alias."
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("JWT private key could not be loaded.", exception);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Extracts the RSA public key used to validate access tokens.
     */
    @Bean
    public RSAPublicKey jwtPublicKey(
            KeyStore jwtKeyStore,
            JwtProperties jwtProperties
    ) {
        try {
            Certificate certificate = jwtKeyStore.getCertificate(jwtProperties.keyAlias());

            if (certificate == null) {
                throw new IllegalStateException(
                        "JWT key store does not contain a certificate for the configured alias."
                );
            }

            if (certificate.getPublicKey() instanceof RSAPublicKey publicKey) {
                return publicKey;
            }

            throw new IllegalStateException(
                    "JWT key store certificate does not contain an RSA public key."
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("JWT public key could not be loaded.", exception);
        }
    }

    /**
     * Creates the encoder that signs access tokens with the configured RSA key pair.
     */
    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey jwtPublicKey,
            RSAPrivateKey jwtPrivateKey,
            JwtProperties jwtProperties
    ) {
        return NimbusJwtEncoder.withKeyPair(jwtPublicKey, jwtPrivateKey)
                .algorithm(SignatureAlgorithm.RS256)
                .jwkPostProcessor(builder -> builder.keyID(jwtProperties.keyAlias()))
                .build();
    }

    /**
     * Creates the decoder that verifies RS256 signatures and required token claims.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            RSAPublicKey jwtPublicKey,
            JwtProperties jwtProperties
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(jwtPublicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer());

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtAudienceValidator(jwtProperties.audience());

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                )
        );

        return decoder;
    }
}