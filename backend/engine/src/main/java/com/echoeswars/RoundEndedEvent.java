package com.echoeswars;

public class RoundEndedEvent extends ReplayEvent {

    private final PlayerLife playersLife;

    public RoundEndedEvent(PlayerLife playersLife) {

        // Initialize the common event information
        super("END", "ROUND_ENDED");

        // Store player health at the end of the round
        this.playersLife = playersLife;
    }

    public PlayerLife getPlayersLife() {
        return playersLife;
    }
}
