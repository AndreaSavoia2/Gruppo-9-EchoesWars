package com.echoeswars;

public class ReplayPlayer {

    private final String playerId;
    private final int initialLife;

    public ReplayPlayer(String playerId, int initialLife) {

        // Store the player's initial match information
        this.playerId = playerId;
        this.initialLife = initialLife;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getInitialLife() {
        return initialLife;
    }
}
