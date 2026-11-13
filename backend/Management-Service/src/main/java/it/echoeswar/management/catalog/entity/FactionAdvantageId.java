package it.echoeswar.management.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FactionAdvantageId implements Serializable {

    @Column(name = "attacker_faction_id", nullable = false)
    private byte attackerFactionId;

    @Column(name = "defender_faction_id", nullable = false)
    private byte defenderFactionId;
}
