package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.DeckCard;
import it.echoeswar.management.deck.entity.DeckCardId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeckCardRepository extends JpaRepository<DeckCard, DeckCardId> {

    /**
     * Loads each distinct deck-card row together with all card data required by {@code CardResponse}.
     */
    @Query("""
            SELECT deckCard
            FROM DeckCard deckCard
            JOIN FETCH deckCard.card card
            JOIN FETCH card.faction
            JOIN FETCH card.school
            JOIN FETCH card.tier
            WHERE deckCard.deck.id = :deckId
            ORDER BY card.id
    """)
    List<DeckCard> findAllByDeckIdWithCardDetails(@Param("deckId") long deckId);

    /**
     * Deletes the current composition before saving the already validated replacement.
     */
    @Modifying
    @Query("DELETE FROM DeckCard deckCard WHERE deckCard.deck.id = :deckId")
    void deleteAllByDeckId(@Param("deckId") long deckId);
}
