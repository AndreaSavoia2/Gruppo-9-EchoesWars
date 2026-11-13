package it.echoeswar.management.deck.entity;

import it.echoeswar.management.catalog.entity.Faction;
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
 * One faction available to a deck.
 */
@Entity
@Table(name = "deck_factions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckFaction {

    @EmbeddedId
    private DeckFactionId id;

    @MapsId("deckId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @MapsId("factionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faction_id", nullable = false)
    private Faction faction;
}
