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
 * Composite identifier for a faction selected by a deck.
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DeckFactionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "deck_id", nullable = false)
    private Long deckId;

    @Column(name = "faction_id", nullable = false)
    private byte factionId;
}
