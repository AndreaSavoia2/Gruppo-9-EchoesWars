package it.echoeswar.management.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Base class for entities whose creation and last-update timestamps are maintained by Hibernate.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity extends CreatedAtEntity {

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
