package com.echoeswars.combat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerLife {

    private final int playerOne;
    private final int playerTwo;

    @JsonCreator
    public PlayerLife(
            @JsonProperty("playerOne") int playerOne,
            @JsonProperty("playerTwo") int playerTwo) {

        // Store the remaining health of both players
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
    }

    public int getPlayerOne() {
        return playerOne;
    }

    public int getPlayerTwo() {
        return playerTwo;
    }
}
