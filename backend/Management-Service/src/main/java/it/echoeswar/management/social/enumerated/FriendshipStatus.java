package it.echoeswar.management.social.enumerated;

/**
 * Lifecycle states of a friendship request and relationship.
 *
 * <p>Values are persisted as strings and must remain aligned with the database constraint.</p>
 */
public enum FriendshipStatus {

    /** Request sent by one user and awaiting the recipient's decision. */
    PENDING,

    /** Request accepted by the recipient; the users are friends. */
    ACCEPTED,

    /** Request rejected by the recipient. */
    REJECTED,

    /** Request cancelled by its sender or friendship removed after acceptance. */
    REMOVED
}
