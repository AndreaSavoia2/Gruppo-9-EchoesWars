package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.Deck;
import it.echoeswar.management.deck.enumerated.DeckStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    /**
     * Counts decks that still occupy one of the owner's active deck slots.
     */
    long countByUserIdAndDeletedAtIsNullAndStatusIn(UUID userId, Collection<DeckStatus> statuses);

    boolean existsByUserIdAndName(UUID userId, String name);
}
