package it.echoeswar.management.catalog.entity;

import it.echoeswar.management.catalog.enumerated.SchoolCode;
import it.echoeswar.management.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schools")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class School extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private byte id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 100, unique = true)
    private SchoolCode code;

    @Column(name = "name_it", nullable = false, length = 100, unique = true)
    private String nameIt;

    @Column(name = "name_en", nullable = false, length = 100, unique = true)
    private String nameEn;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
