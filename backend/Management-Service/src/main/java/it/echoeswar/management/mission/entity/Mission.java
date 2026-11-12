package it.echoeswar.management.mission.entity;

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
 * A catalog mission that can be completed once by each user.
 */
@Entity
@Table(name = "missions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mission extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "name_it", nullable = false, length = 150, unique = true)
    private String nameIt;

    @Column(name = "name_en", nullable = false, length = 150, unique = true)
    private String nameEn;

    @Column(name = "description_it", columnDefinition = "TEXT")
    private String descriptionIt;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Builder.Default
    @Column(name = "reward_credits", nullable = false)
    private long rewardCredits = 0L;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
