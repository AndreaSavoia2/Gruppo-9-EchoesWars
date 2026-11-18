package it.echoeswar.management.pack.entity;

import it.echoeswar.management.common.persistence.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable commercial configuration of a {@link GamePackage}.
 *
 * <p>Once created, the package, version number, price, and publication date
 * define the historical version and cannot change. Only the availability
 * lifecycle can change, by retiring the version.</p>
 */
@Entity
@Table(name = "package_versions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageVersion extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false, updatable = false)
    private GamePackage gamePackage;

    @Column(name = "version_number", nullable = false, updatable = false)
    private int versionNumber;

    @Column(name = "price_credits", nullable = false, updatable = false)
    private long priceCredits;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "published_at", updatable = false)
    private LocalDateTime publishedAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    /**
     * Retires this version without changing its historical commercial configuration.
     *
     * @param retiredAt time at which the version stopped being available for purchase
     */
    public void retire(Instant retiredAt) {
        this.retiredAt = Objects.requireNonNull(retiredAt, "retiredAt must not be null.");
        this.active = false;
    }

}
