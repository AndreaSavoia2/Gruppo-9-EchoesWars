package it.echoeswar.management.pack.entity;

import it.echoeswar.management.common.persistence.CreatedAtEntity;
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

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * A versioned commercial configuration of a {@link GamePackage}.
 */
@Entity
@Table(name = "package_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageVersion extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private GamePackage gamePackage;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "price_credits", nullable = false)
    private long priceCredits;

    @Column(name = "pull_rule_code", nullable = false, length = 100)
    private String pullRuleCode;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "retired_at")
    private Instant retiredAt;
}
