package it.echoeswar.management.catalog.entity;

import it.echoeswar.management.catalog.enumerated.EffectCode;
import it.echoeswar.management.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "effects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Effect extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 100, unique = true)
    private EffectCode code;

    @Column(name = "name_it", nullable = false, length = 150, unique = true)
    private String nameIt;

    @Column(name = "name_en", nullable = false, length = 150, unique = true)
    private String nameEn;

    @Column(name = "description_it", columnDefinition = "TEXT")
    private String descriptionIt;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
