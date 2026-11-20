package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.Deck;
import it.echoeswar.management.deck.enumerated.DeckStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    /**
     * Counts non-deleted READY and ARCHIVED decks that occupy one of the owner's active deck slots.
     */
    long countByUserIdAndDeletedAtIsNullAndStatusIn(UUID userId, Collection<DeckStatus> statuses);

    /**
     * Finds non-deleted decks owned by a user for the requested visible statuses.
     */
    List<Deck> findAllByUserIdAndDeletedAtIsNullAndStatusInOrderByCreatedAtDesc(
            UUID userId,
            Collection<DeckStatus> statuses
    );

    /**
     * Finds one visible deck only when it belongs to the requested user.
     *
     * <p>The rule is fetched because the detail response exposes its identifier and code.</p>
     */
    @EntityGraph(attributePaths = "deckRule")
    Optional<Deck> findByIdAndUserIdAndDeletedAtIsNullAndStatusIn(
            Long deckId,
            UUID userId,
            Collection<DeckStatus> statuses
    );

    /**
     * Checks whether a non-deleted deck owned by the user already uses the requested name.
     */
    boolean existsByUserIdAndNameAndDeletedAtIsNull(UUID userId, String name);

    /**
     * Checks whether another non-deleted deck owned by the same user already uses the requested name.
     */
    boolean existsByUserIdAndNameAndIdNotAndDeletedAtIsNull(UUID userId, String name, Long deckId);
}
