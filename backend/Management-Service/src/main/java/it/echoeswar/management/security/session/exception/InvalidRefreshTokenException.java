package it.echoeswar.management.security.session.exception;

/**
 * Indicates that a refresh token cannot be used.
 */
public final class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid.");
    }
}