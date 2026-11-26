package it.echoeswar.management.social.repository;

import it.echoeswar.management.social.entity.Friendship;
import it.echoeswar.management.social.enumerated.FriendshipStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for canonical friendship pairs and friendship requests.
 */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Checks whether a relationship already exists for the user pair in the canonical order
     * required by the {@code friendships} table.
     *
     * @param canonicalFirstUserId identifier of the first user in canonical order
     * @param canonicalSecondUserId identifier of the second user in canonical order
     * @return {@code true} when a friendship record already exists for the pair
     */
    boolean existsByFirstUser_IdAndSecondUser_Id(
            UUID canonicalFirstUserId,
            UUID canonicalSecondUserId
    );

    /**
     * Loads and locks a friendship together with the users required to process a request transition.
     *
     * @param friendshipId identifier of the friendship to lock
     * @return the locked friendship and its participants, when it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select friendship
            from Friendship friendship
            join fetch friendship.firstUser
            join fetch friendship.secondUser
            join fetch friendship.requestedByUser
            where friendship.id = :friendshipId
            """)
    Optional<Friendship> findByIdForUpdateWithUsers(@Param("friendshipId") Long friendshipId);

    /**
     * Finds pending requests received by a user, regardless of that user's position
     * in the canonical friendship pair.
     *
     * @param recipientUserId identifier of the request recipient
     * @param status status required for the returned requests
     * @return matching friendship requests, ordered from newest to oldest
     */
    @Query("""
            select friendship
            from Friendship friendship
            join fetch friendship.requestedByUser requester
            where friendship.status = :status
              and requester.id <> :recipientUserId
              and (
                  friendship.firstUser.id = :recipientUserId
                  or friendship.secondUser.id = :recipientUserId
              )
            order by friendship.requestedAt desc, friendship.id desc
            """)
    List<Friendship> findReceivedRequestsByRecipientUserIdAndStatus(
            @Param("recipientUserId") UUID recipientUserId,
            @Param("status") FriendshipStatus status
    );
}
