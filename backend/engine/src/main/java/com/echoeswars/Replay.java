package com.echoeswars;

import java.util.ArrayList;

public class Replay {

    private final ArrayList<ReplayPlayer> players;
    private final ArrayList<ReplayRound> rounds;

    public Replay(ArrayList<ReplayPlayer> players) {

        // Store the initial player information
        this.players = new ArrayList<>(players);
        // Initialize the round history
        this.rounds = new ArrayList<>();
    }

    public ArrayList<ReplayPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    public void addRound(ReplayRound replayRound) {

        // Store a completed round in the replay
        rounds.add(replayRound);
    }

    public ArrayList<ReplayRound> getRounds() {

        // Return a copy of the round list
        return new ArrayList<>(rounds);
    }
}
