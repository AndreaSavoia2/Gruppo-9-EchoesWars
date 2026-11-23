package it.echoeswar.management.deck.enumerated;

/**
 * Lifecycle state of a user deck.
 */
public enum DeckStatus {
    /**
     * The deck is active and can be used in play.
     */
    READY,

    /**
     * The deck remains active but cannot be used in play.
     */
    ARCHIVED,

    /**
     * The deck has been deleted and no longer occupies an active deck slot.
     */
    DISABLED
}
