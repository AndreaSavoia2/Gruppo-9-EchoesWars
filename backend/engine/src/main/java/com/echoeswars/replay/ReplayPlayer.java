package com.echoeswars.replay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplayPlayer {

    private final String playerId;
    private final int initialLife;

    @JsonCreator
    public ReplayPlayer(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("initialLife") int initialLife) {

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
