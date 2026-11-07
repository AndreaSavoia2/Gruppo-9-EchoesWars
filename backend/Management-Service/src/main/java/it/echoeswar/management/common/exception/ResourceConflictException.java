package it.echoeswar.management.common.exception;

/**
 * Indicates that a request conflicts with the current application state.
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
