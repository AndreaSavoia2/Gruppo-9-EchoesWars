package it.echoeswar.management.game.entity;

import com.fasterxml.jackson.databind.JsonNode;
import it.echoeswar.management.common.persistence.CreatedAtEntity;
import it.echoeswar.management.game.enumerated.MatchResult;
import it.echoeswar.management.game.enumerated.MatchStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Persisted result and replay input of a challenge simulation.
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rules_version_id", nullable = false)
    private GameRuleVersion rulesVersion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchStatus status = MatchStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 30)
    private MatchResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_user_id")
    private User winnerUser;

    @Column(name = "player_one_final_life")
    private Integer playerOneFinalLife;

    @Column(name = "player_two_final_life")
    private Integer playerTwoFinalLife;

    @Column(name = "total_rounds")
    private Integer totalRounds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot", nullable = false)
    private JsonNode inputSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "replay")
    private JsonNode replay;

    @Column(name = "engine_version", length = 100)
    private String engineVersion;

    @Column(name = "random_seed", length = 255)
    private String randomSeed;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
