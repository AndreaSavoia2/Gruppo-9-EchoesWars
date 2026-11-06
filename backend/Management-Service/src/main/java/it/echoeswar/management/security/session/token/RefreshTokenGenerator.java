package it.echoeswar.management.security.session.token;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Generates cryptographically secure opaque refresh tokens and their hashes.
 */
@Component
public class RefreshTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private static final Base64.Encoder TOKEN_ENCODER =
            Base64.getUrlEncoder().withoutPadding();

    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final SecureRandom secureRandom;

    public RefreshTokenGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public GeneratedRefreshToken generate() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        try {
            String rawToken = TOKEN_ENCODER.encodeToString(tokenBytes);

            return new GeneratedRefreshToken(
                    rawToken,
                    hash(rawToken)
            );
        } finally {
            Arrays.fill(tokenBytes, (byte) 0);
        }
    }

    public String hash(String rawToken) {
        Objects.requireNonNull(rawToken, "rawToken must not be null");

        if (rawToken.isBlank()) {
            throw new IllegalArgumentException("rawToken must not be blank");
        }

        byte[] rawTokenBytes = rawToken.getBytes(StandardCharsets.UTF_8);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(rawTokenBytes);

            try {
                return HEX_FORMAT.formatHex(hashBytes);
            } finally {
                Arrays.fill(hashBytes, (byte) 0);
            }
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available in this Java runtime.",
                    exception
            );
        } finally {
            Arrays.fill(rawTokenBytes, (byte) 0);
        }
    }
}