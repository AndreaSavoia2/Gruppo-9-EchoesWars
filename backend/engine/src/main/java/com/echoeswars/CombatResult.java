package com.echoeswars;

import java.util.ArrayList;

public class CombatResult {

    private final ArrayList<CombatCardResult> cards;
    private final PlayerLife playerDamage;

    public CombatResult(
            CombatCardResult firstCardResult,
            CombatCardResult secondCardResult,
            int firstPlayerDamage,
            int secondPlayerDamage
    ) {
        // Store the combat results of both cards
        this.cards = new ArrayList<>();

        this.cards.add(firstCardResult);
        this.cards.add(secondCardResult);

        // Store the damage received by both players
        this.playerDamage = new PlayerLife(
                firstPlayerDamage,
                secondPlayerDamage
        );
    }

    public ArrayList<CombatCardResult> getCards() {
        return new ArrayList<>(cards);
    }

    public PlayerLife getPlayerDamage() {
        return playerDamage;
    }
}
