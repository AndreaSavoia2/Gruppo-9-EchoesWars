package it.echoeswar.management.catalog.entity;

import it.echoeswar.management.common.persistence.CreatedAtEntity;
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

/** Attack bonus granted to an attacking faction against a defending faction. */
@Entity
@Table(name = "faction_advantages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactionAdvantage extends CreatedAtEntity {

    @EmbeddedId
    private FactionAdvantageId id;

    @MapsId("attackerFactionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attacker_faction_id", nullable = false)
    private Faction attackerFaction;

    @MapsId("defenderFactionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defender_faction_id", nullable = false)
    private Faction defenderFaction;

    @Builder.Default
    @Column(name = "attack_bonus", nullable = false)
    private byte attackBonus = 1;
}
