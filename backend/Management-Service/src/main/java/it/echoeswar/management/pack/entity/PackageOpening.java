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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Outcome metadata for the opening of a package purchase.
 */
@Entity
@Table(name = "package_openings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageOpening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_purchase_id", nullable = false)
    private PackagePurchase packagePurchase;

    @CreationTimestamp
    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt;

    @Column(name = "random_seed", length = 255)
    private String randomSeed;

    @Column(name = "opening_number", nullable = false)
    private long openingNumber;

    @Column(name = "cycle_position", nullable = false)
    private short cyclePosition;

    @Builder.Default
    @Column(name = "was_guaranteed", nullable = false)
    private boolean wasGuaranteed = false;
}
