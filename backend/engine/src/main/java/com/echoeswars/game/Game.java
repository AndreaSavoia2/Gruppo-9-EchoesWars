package com.echoeswars.game;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.combat.PlayerLife;
import com.echoeswars.effects.EffectResolver;
import com.echoeswars.model.Player;
import com.echoeswars.replay.Replay;
import com.echoeswars.replay.ReplayGameState;
import com.echoeswars.replay.ReplayPlayer;
import com.echoeswars.replay.ReplayRound;
import com.echoeswars.result.MatchResult;

public class Game {

    private Player player1;
    private Player player2;

    private int round;

    private static final int MAX_ROUNDS = 100;

    private final String matchId;
    private final String rulesVersionId;

    private final Replay replay;

    private final RoundManager roundManager;

    private BiConsumer<Integer, ReplayGameState> roundStateObserver;

    private static final String ENGINE_VERSION = "1.0.0";

    private String winnerId;
    private MatchStatus status;

    public Game(
            String matchId,
            String rulesVersionId,
            Player player1,
            Player player2) {

        // Store match metadata
        this.matchId = matchId;
        this.rulesVersionId = rulesVersionId;

        // Store the participating players
        this.player1 = player1;
        this.player2 = player2;

        this.roundManager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        // Capture player information before the match starts
        ArrayList<ReplayPlayer> replayPlayers = new ArrayList<>();

        replayPlayers.add(
                new ReplayPlayer(
                        player1.getPlayerId(),
                        player1.getHealth()));

        replayPlayers.add(
                new ReplayPlayer(
                        player2.getPlayerId(),
                        player2.getHealth()));

        // Initialize the match replay
        this.replay = new Replay(replayPlayers);

        // Capture the initial state before the first round.
        this.replay.setInitialState(
                new ReplayGameState(player1, player2));

        // Initialize the match state
        this.round = 1;
        this.winnerId = null;
        this.status = MatchStatus.IN_PROGRESS;
    }

    public void play() {

        // Continue until a player is defeated or the round limit is reached.
        while (player1.canContinue()
                && player2.canContinue()
                && round <= MAX_ROUNDS) {

            // Execute one round and collect its replay.
            ReplayRound replayRound = roundManager.playRound(
                    round,
                    player1,
                    player2);

            replay.addRound(replayRound);

            // Notify the observer without storing snapshots in the replay.
            if (roundStateObserver != null) {
                roundStateObserver.accept(
                        round,
                        new ReplayGameState(player1, player2));
            }

            round++;
        }

        // Determine the final match result.
        printResult();

        // Capture the complete state after the match ends.
        replay.setFinalState(
                new ReplayGameState(player1, player2));
    }

    private void printResult() {

        // Determine and store the match outcome
        if (!player1.canContinue() && !player2.canContinue()) {

            status = MatchStatus.DRAW;
            winnerId = null;

        } else if (!player1.canContinue()) {

            status = MatchStatus.COMPLETED;
            winnerId = player2.getPlayerId();

        } else if (!player2.canContinue()) {

            status = MatchStatus.COMPLETED;
            winnerId = player1.getPlayerId();

        } else {

            status = MatchStatus.ROUND_LIMIT;
            winnerId = null;
        }
    }

    public String getWinnerId() {
        return winnerId;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public int getRoundsPlayed() {
        return round - 1;
    }

    public MatchResult getMatchResult() {

        // Prevent retrieving a result before the match finishes
        if (status == MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "The match has not finished yet.");
        }

        // Capture the final health of both players
        PlayerLife currentLife = new PlayerLife(
                player1.getHealth(),
                player2.getHealth());

        // Create a snapshot of the final match information
        return new MatchResult(
                matchId,
                rulesVersionId,
                ENGINE_VERSION,
                status,
                winnerId,
                round - 1,
                currentLife,
                replay);
    }

    /**
     * Registers an optional observer for completed round states.
     * Intended for verification and diagnostics.
     */
    public void setRoundStateObserver(
            BiConsumer<Integer, ReplayGameState> observer) {

        if (status != MatchStatus.IN_PROGRESS || round != 1) {
            throw new IllegalStateException(
                    "The observer must be configured before the match starts.");
        }

        this.roundStateObserver = observer;
    }

}
