package it.echoeswar.management.user.exception;

/**
 * Indicates that the application has no active deck rule configured.
 */
public final class AccountConflictException extends RuntimeException {

    public AccountConflictException() {
        super( "An account with the provided username or email already exists.");
    }
}
