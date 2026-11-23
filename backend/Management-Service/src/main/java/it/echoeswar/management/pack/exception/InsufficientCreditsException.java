package it.echoeswar.management.pack.exception;

/**
 * Indicates that the application has no active deck rule configured.
 */
public final class InsufficientCreditsException extends RuntimeException {

    public InsufficientCreditsException() {
        super("Insufficient credits to purchase this package.");
    }
}
