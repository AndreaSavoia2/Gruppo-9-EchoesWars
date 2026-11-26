package it.echoeswar.management.social.entity;

import it.echoeswar.management.social.enumerated.FriendshipStatus;
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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Persistent relationship between two users.
 *
 * <p>The two participants are stored in canonical UUID order: {@code firstUser} is the user
 * mapped to {@code user_a_id}, and {@code secondUser} is mapped to {@code user_b_id}. The user
 * who initiated the request is recorded separately so that request actions can be authorized
 * independently of that canonical ordering.</p>
 */
@Entity
@Table(name = "friendships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    /** Primary key of the friendship relationship. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** First participant in the canonical database ordering. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User firstUser;

    /** Second participant in the canonical database ordering. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User secondUser;

    /** User who created the friendship request. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedByUser;

    /** Current lifecycle state of the relationship. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    /** Timestamp at which the request was created. */
    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    /** Timestamp at which the recipient accepted the request, if applicable. */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    /** Timestamp at which the recipient rejected the request, if applicable. */
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    /** Timestamp at which the request or relationship was removed, if applicable. */
    @Column(name = "removed_at")
    private LocalDateTime removedAt;
}
