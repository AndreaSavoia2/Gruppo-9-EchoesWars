package it.echoeswar.management.social.enumerated;

/**
 * Lifecycle states of a card-trade proposal.
 *
 * <p>Values are persisted as strings and must remain aligned with the database constraint.</p>
 */
public enum TradeStatus {

    /** Trade is awaiting the actions required to complete it. */
    OPEN,

    /** Trade completed and its card exchange was applied. */
    COMPLETED,

    /** Trade was rejected by one of its participants. */
    REJECTED,

    /** Trade was cancelled before completion. */
    CANCELLED,

    /** Trade expired before completion. */
    EXPIRED
}
