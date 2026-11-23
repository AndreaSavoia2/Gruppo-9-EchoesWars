package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.echoeswars.combat.PlayerLife;
import com.echoeswars.replay.ReplayEvent;

public class RoundEndedEvent extends ReplayEvent {

    private final PlayerLife playersLife;

    @JsonCreator
    public RoundEndedEvent(@JsonProperty("playersLife") PlayerLife playersLife) {

        // Initialize the common event information
        super("END", "ROUND_ENDED");

        // Store player health at the end of the round
        this.playersLife = playersLife;
    }

    public PlayerLife getPlayersLife() {
        return playersLife;
    }
}
