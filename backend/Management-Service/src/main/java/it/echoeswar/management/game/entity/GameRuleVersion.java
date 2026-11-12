package it.echoeswar.management.game.entity;

import it.echoeswar.management.common.persistence.CreatedAtEntity;
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
 * Immutable-versioned game-rule configuration used to reproduce a match.
 */
@Entity
@Table(name = "game_rule_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRuleVersion extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "version", nullable = false)
    private int version;

    @Builder.Default
    @Column(name = "initial_life", nullable = false)
    private int initialLife = 20;

    @Builder.Default
    @Column(name = "max_rounds", nullable = false)
    private int maxRounds = 100;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
