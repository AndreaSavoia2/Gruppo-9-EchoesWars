package it.echoeswar.management.pack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Immutable draw rules associated with a specific commercial version.
 *
 * <p>Once created, a rule must not be modified or reassigned. To change a
 * package's behavior, create a new {@link PackageVersion} with a new rule.
 * Purchases that reference previous versions therefore retain the behavior
 * defined at the time of purchase.</p>
 */
@Entity
@Immutable
@Table(name = "package_pull_rules")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagePullRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "package_version_id",
            nullable = false,
            unique = true
    )
    private PackageVersion packageVersion;

    @Positive
    @Column(name = "cards_per_package", nullable = false)
    private int cardsPerPackage;

    @Min(0)
    @Column(name = "guaranteed_common_cards", nullable = false)
    private int guaranteedCommonCards;

    @Min(0)
    @Column(name = "guaranteed_rare_cards", nullable = false)
    private int guaranteedRareCards;

    @Positive
    @Column(name = "cycle_length", nullable = false)
    private int cycleLength;

    @Min(0)
    @Max(100)
    @Column(name = "random_common_threshold", nullable = false)
    private int randomCommonThreshold;

    @Min(0)
    @Max(100)
    @Column(name = "random_rare_threshold", nullable = false)
    private int randomRareThreshold;

    /**
     * Guaranteed cards must leave at least one variable slot. Variable slots use
     * the random thresholds or the end-of-cycle legendary guarantee.
     */
    @AssertTrue(message = "The pull rule must leave at least one variable card slot.")
    public boolean isCardDistributionValid() {
        return cardsPerPackage > (long) guaranteedCommonCards + guaranteedRareCards;
    }

    /**
     * The second threshold is cumulative and must therefore be greater than or
     * equal to the first.
     */
    @AssertTrue(message = "The rare threshold must be greater than or equal to the common threshold.")
    public boolean isRandomThresholdsOrdered() {
        return randomCommonThreshold <= randomRareThreshold;
    }
}
