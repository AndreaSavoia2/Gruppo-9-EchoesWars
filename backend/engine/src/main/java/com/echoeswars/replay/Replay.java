package com.echoeswars.replay;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Replay {

    private final ArrayList<ReplayPlayer> players;
    private final ArrayList<ReplayRound> rounds;

    private ReplayGameState initialState;
    private ReplayGameState finalState;

    @JsonCreator
    public Replay(
            @JsonProperty("players") ArrayList<ReplayPlayer> players,
            @JsonProperty("rounds") ArrayList<ReplayRound> rounds) {

        if (players == null || rounds == null) {
            throw new IllegalArgumentException(
                    "Replay players and rounds cannot be null.");
        }

        this.players = new ArrayList<>(players);
        this.rounds = new ArrayList<>(rounds);
    }

    public Replay(ArrayList<ReplayPlayer> players) {
        this(players, new ArrayList<>());
    }

    public ArrayList<ReplayPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    public void addRound(ReplayRound replayRound) {
        rounds.add(replayRound);
    }

    public ArrayList<ReplayRound> getRounds() {
        return new ArrayList<>(rounds);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ReplayGameState getInitialState() {
        return initialState;
    }

    public void setInitialState(ReplayGameState initialState) {
        if (initialState == null) {
            throw new IllegalArgumentException(
                    "Initial state cannot be null.");
        }

        if (this.initialState != null) {
            throw new IllegalStateException(
                    "Initial state has already been set.");
        }

        this.initialState = initialState;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ReplayGameState getFinalState() {
        return finalState;
    }

    public void setFinalState(ReplayGameState finalState) {
        if (finalState == null) {
            throw new IllegalArgumentException(
                    "Final state cannot be null.");
        }

        this.finalState = finalState;
    }
}
