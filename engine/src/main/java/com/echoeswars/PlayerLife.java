package com.echoeswars;

public class PlayerLife {

    private final int playerOne;
    private final int playerTwo;

    public PlayerLife(int playerOne, int playerTwo) {

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
