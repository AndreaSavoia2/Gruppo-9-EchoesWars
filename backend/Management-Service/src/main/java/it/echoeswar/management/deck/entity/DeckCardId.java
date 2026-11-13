package it.echoeswar.management.deck.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite identifier for one card entry in a deck.
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DeckCardId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "deck_id", nullable = false)
    private Long deckId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;
}
