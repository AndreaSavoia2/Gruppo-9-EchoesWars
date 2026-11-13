package it.echoeswar.management.catalog.entity;

import it.echoeswar.management.catalog.enumerated.EchoesCode;
import it.echoeswar.management.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "factions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Faction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private byte id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private EchoesCode code;

    @Column(name = "name_it", nullable = false, length = 100, unique = true)
    private String nameIt;

    @Column(name = "name_en", nullable = false, length = 100, unique = true)
    private String nameEn;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "color_hex", nullable = false, length = 7, columnDefinition = "char(7)")
    private String colorHex;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
