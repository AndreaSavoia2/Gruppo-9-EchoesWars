
package com.echoeswars.combat;

import java.util.ArrayList;

public class CombatResult {

    private final ArrayList<CombatCardResult> cards;
    private final PlayerLife playerDamage;

    private final boolean firstAttackDestroyedOpponent;
    private final boolean secondAttackDestroyedOpponent;

    public CombatResult(
            CombatCardResult firstCardResult,
            CombatCardResult secondCardResult,
            int firstPlayerDamage,
            int secondPlayerDamage) {

        this(
                firstCardResult,
                secondCardResult,
                firstPlayerDamage,
                secondPlayerDamage,
                false,
                false);
    }

    public CombatResult(
            CombatCardResult firstCardResult,
            CombatCardResult secondCardResult,
            int firstPlayerDamage,
            int secondPlayerDamage,
            boolean firstAttackDestroyedOpponent,
            boolean secondAttackDestroyedOpponent) {

        this.cards = new ArrayList<>();

        this.cards.add(firstCardResult);
        this.cards.add(secondCardResult);

        this.playerDamage = new PlayerLife(
                firstPlayerDamage,
                secondPlayerDamage);

        this.firstAttackDestroyedOpponent = firstAttackDestroyedOpponent;

        this.secondAttackDestroyedOpponent = secondAttackDestroyedOpponent;
    }

    public ArrayList<CombatCardResult> getCards() {
        return new ArrayList<>(cards);
    }

    public PlayerLife getPlayerDamage() {
        return playerDamage;
    }

    public boolean isFirstAttackDestroyedOpponent() {
        return firstAttackDestroyedOpponent;
    }

    public boolean isSecondAttackDestroyedOpponent() {
        return secondAttackDestroyedOpponent;
    }

    public int getDamageToFirstCardDefense() {
        return cards.get(1)
                .getDamageBreakdown()
                .get("defenseDamage");
    }

    public int getDamageToSecondCardDefense() {
        return cards.get(0)
                .getDamageBreakdown()
                .get("defenseDamage");
    }

}
