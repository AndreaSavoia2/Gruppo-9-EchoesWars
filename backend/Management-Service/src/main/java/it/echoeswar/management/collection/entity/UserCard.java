package it.echoeswar.management.collection.entity;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.common.persistence.AuditableEntity;
import it.echoeswar.management.user.entity.User;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Number of copies of a catalog card currently owned by a user.
 */
@Entity
@Table(name = "user_cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCard extends AuditableEntity {

    @EmbeddedId
    private UserCardId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("cardId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
