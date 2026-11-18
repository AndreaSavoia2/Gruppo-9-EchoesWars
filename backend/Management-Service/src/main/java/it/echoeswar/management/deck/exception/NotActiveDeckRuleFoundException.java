package it.echoeswar.management.deck.exception;

/**
 * Indicates that the application has no active deck rule configured.
 */
public final class NotActiveDeckRuleFoundException extends RuntimeException {

    public NotActiveDeckRuleFoundException() {
        super("No active deck rule found.");
    }
}
