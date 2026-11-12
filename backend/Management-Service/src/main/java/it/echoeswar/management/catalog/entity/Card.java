package it.echoeswar.management.catalog.entity;

import it.echoeswar.management.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private long id;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "name_it", nullable = false, length = 150)
    private String nameIt;

    @Column(name = "name_en", nullable = false, length = 150)
    private String nameEn;

    @Column(name = "description_it", columnDefinition = "TEXT")
    private String descriptionIt;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "attack", nullable = false)
    private int attack;

    @Column(name = "defense", nullable = false)
    private int defense;

    @Column(name = "play_cost", nullable = false)
    private int playCost;

    @Builder.Default
    @Column(name = "max_copies_per_deck", nullable = false)
    private int maxCopiesPerDeck = 3;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faction_id", nullable = false)
    private Faction faction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effect_id")
    private Effect effect;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
