package it.echoeswar.management.deck.entity;

import it.echoeswar.management.catalog.entity.Card;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Quantity of a given catalog card assigned to a deck.
 */
@Entity
@Table(name = "deck_cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckCard {

    @EmbeddedId
    private DeckCardId id;

    @MapsId("deckId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @MapsId("cardId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
