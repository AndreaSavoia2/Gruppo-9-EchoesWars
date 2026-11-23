package it.echoeswar.management.social.repository;

import it.echoeswar.management.social.entity.Friendship;
import it.echoeswar.management.social.enumerated.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Verifica se esiste già una relazione per la coppia di utenti nell'ordine
     * canonico previsto dalla tabella {@code friendships}.
     */
    boolean existsByFirstUser_IdAndSecondUser_Id(
            UUID canonicalFirstUserId,
            UUID canonicalSecondUserId
    );

    /**
     * Finds pending requests received by a user, regardless of that user's position
     * in the canonical friendship pair.
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
