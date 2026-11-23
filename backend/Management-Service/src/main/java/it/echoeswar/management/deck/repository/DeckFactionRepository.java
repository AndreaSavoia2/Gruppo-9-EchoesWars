package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.DeckFaction;
import it.echoeswar.management.deck.entity.DeckFactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DeckFactionRepository extends JpaRepository<DeckFaction, DeckFactionId> {

    /**
     * Returns the explicitly selected factions in a stable identifier order.
     */
    @Query("""
            SELECT deckFaction
            FROM DeckFaction deckFaction
            JOIN FETCH deckFaction.faction
            WHERE deckFaction.deck.id = :deckId
            ORDER BY deckFaction.id.factionId
            """)
    List<DeckFaction> findAllByDeckIdOrderByFactionId(@Param("deckId") long deckId);

    /**
     * Loads the factions associated with multiple decks in a single query, avoiding one query per summary.
     */
    @Query("""
            SELECT deckFaction
            FROM DeckFaction deckFaction
            JOIN FETCH deckFaction.faction
            WHERE deckFaction.deck.id IN :deckIds
            ORDER BY deckFaction.deck.id, deckFaction.id.factionId
            """)
    List<DeckFaction> findAllByDeckIdInWithFactionDetails(
            @Param("deckIds") Collection<Long> deckIds
    );
}
