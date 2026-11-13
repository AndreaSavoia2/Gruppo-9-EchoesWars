package it.echoeswar.management.security.auth.password;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class PasswordPolicy {

    private static final int MINIMUM_PASSWORD_LENGTH = 12;
    private static final int MAXIMUM_BCRYPT_PASSWORD_BYTES = 72;

    /**
     * Validates a password before it is stored or changed.
     */
    public void validateForRegistration(String rawPassword) {
        validateNotBlank(rawPassword);

        if (rawPassword.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters."
            );
        }

        validateBcryptLength(rawPassword);
    }

    /**
     * Validates a password before it is passed to BCrypt for authentication.
     */
    public void validateForAuthentication(String rawPassword) {
        validateNotBlank(rawPassword);
        validateBcryptLength(rawPassword);
    }

    private void validateNotBlank(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Password must not be blank."
            );
        }
    }

    private void validateBcryptLength(String rawPassword) {
        byte[] passwordBytes = rawPassword.getBytes(StandardCharsets.UTF_8);

        try {
            if (passwordBytes.length > MAXIMUM_BCRYPT_PASSWORD_BYTES) {
                throw new IllegalArgumentException(
                        "Password must not exceed 72 UTF-8 bytes."
                );
            }
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }
}