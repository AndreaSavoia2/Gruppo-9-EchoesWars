package it.echoeswar.management.game.entity;

import it.echoeswar.management.common.persistence.CreatedAtEntity;
import it.echoeswar.management.deck.entity.Deck;
import it.echoeswar.management.game.enumerated.ChallengeStatus;
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

/**
 * Invitation to play issued by a user with a selected challenger deck.
 */
@Entity
@Table(name = "challenges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenger_user_id", nullable = false)
    private User challengerUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenged_user_id", nullable = false)
    private User challengedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenger_deck_id", nullable = false)
    private Deck challengerDeck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenged_deck_id")
    private Deck challengedDeck;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChallengeStatus status = ChallengeStatus.PENDING;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
