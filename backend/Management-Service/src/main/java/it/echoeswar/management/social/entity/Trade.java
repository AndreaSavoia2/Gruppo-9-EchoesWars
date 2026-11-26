package it.echoeswar.management.social.entity;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.common.persistence.AuditableEntity;
import it.echoeswar.management.social.enumerated.TradeStatus;
import it.echoeswar.management.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Card-trade proposal between the participants of an accepted friendship.
 */
@Entity
@Table(name = "trades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade extends AuditableEntity {

    /** Primary key of the trade proposal. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** Accepted friendship that authorizes this trade. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friendship_id", nullable = false)
    private Friendship friendship;

    /** User who initiated the trade. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposer_user_id", nullable = false)
    private User proposerUser;

    /** Other friendship participant who may accept or reject the trade. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    /** Card offered by the proposer. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposer_card_id", nullable = false)
    private Card proposerCard;

    /** Card requested from the recipient, when the proposal specifies one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_card_id")
    private Card recipientCard;

    /** Timestamp at which the proposer accepted the trade terms. */
    @Column(name = "proposer_accepted_at")
    private LocalDateTime proposerAcceptedAt;

    /** Timestamp at which the recipient accepted the trade terms. */
    @Column(name = "recipient_accepted_at")
    private Instant recipientAcceptedAt;

    /** Current lifecycle state of the trade. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TradeStatus status = TradeStatus.OPEN;

    /** Timestamp at which the trade completed successfully. */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Timestamp at which either participant rejected the trade. */
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    /** Timestamp at which the trade was cancelled. */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /** Timestamp after which an open trade is no longer valid. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
