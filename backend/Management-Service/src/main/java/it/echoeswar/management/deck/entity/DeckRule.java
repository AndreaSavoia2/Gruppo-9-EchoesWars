package it.echoeswar.management.deck.entity;

import it.echoeswar.management.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A versionless ruleset that constrains how a deck may be assembled.
 */
@Entity
@Table(name = "deck_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckRule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "exact_card_count", nullable = false)
    private int exactCardCount = 30;

    @Builder.Default
    @Column(name = "max_total_cost", nullable = false)
    private int maxTotalCost = 100;

    @Builder.Default
    @Column(name = "max_copies_same_card", nullable = false)
    private int maxCopiesSameCard = 3;

    @Builder.Default
    @Column(name = "required_faction_count", nullable = false)
    private int requiredFactionCount = 2;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
