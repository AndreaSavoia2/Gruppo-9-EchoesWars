
package com.echoeswars.effects;

import com.echoeswars.model.Card;
import com.echoeswars.model.Player;

public class EffectContext {

    private final Card sourceCard;
    private final Player owner;
    private final Player opponent;
    private final boolean opponentEnteredThisRound;
    private final int ownerDiscardCountAtEntry;
    private final int attackDamageToDefense;
    private final boolean attackDestroyedOpponent;

    public EffectContext(
            Card sourceCard,
            Player owner,
            Player opponent) {

        this(sourceCard, owner, opponent, false,
                requireOwner(owner).getDiscardPileSize(), 0);
    }

    public EffectContext(
            Card sourceCard,
            Player owner,
            Player opponent,
            boolean opponentEnteredThisRound) {

        this(sourceCard, owner, opponent,
                opponentEnteredThisRound,
                requireOwner(owner).getDiscardPileSize(), 0);
    }

    public EffectContext(
            Card sourceCard,
            Player owner,
            Player opponent,
            boolean opponentEnteredThisRound,
            int ownerDiscardCountAtEntry) {

        this(sourceCard, owner, opponent,
                opponentEnteredThisRound,
                ownerDiscardCountAtEntry, 0);
    }

    public EffectContext(
            Card sourceCard,
            Player owner,
            Player opponent,
            boolean opponentEnteredThisRound,
            int ownerDiscardCountAtEntry,
            int attackDamageToDefense) {

        if (sourceCard == null || owner == null
                || opponent == null) {
            throw new IllegalArgumentException(
                    "Effect context arguments cannot be null.");
        }

        if (ownerDiscardCountAtEntry < 0
                || attackDamageToDefense < 0) {
            throw new IllegalArgumentException(
                    "Effect context values cannot be negative.");
        }

        this.sourceCard = sourceCard;
        this.owner = owner;
        this.opponent = opponent;
        this.opponentEnteredThisRound = opponentEnteredThisRound;
        this.ownerDiscardCountAtEntry = ownerDiscardCountAtEntry;
        this.attackDamageToDefense = attackDamageToDefense;
        this.attackDestroyedOpponent = false;
    }

    public EffectContext(
            Card sourceCard,
            Player owner,
            Player opponent,
            boolean opponentEnteredThisRound,
            int ownerDiscardCountAtEntry,
            int attackDamageToDefense,
            boolean attackDestroyedOpponent) {

        this.sourceCard = sourceCard;
        this.owner = owner;
        this.opponent = opponent;
        this.opponentEnteredThisRound = opponentEnteredThisRound;
        this.ownerDiscardCountAtEntry = ownerDiscardCountAtEntry;
        this.attackDamageToDefense = attackDamageToDefense;
        this.attackDestroyedOpponent = attackDestroyedOpponent;
    }

    public boolean hasAttackDestroyedOpponent() {
        return attackDestroyedOpponent;
    }

    private static Player requireOwner(Player owner) {
        if (owner == null) {
            throw new IllegalArgumentException(
                    "Effect context arguments cannot be null.");
        }
        return owner;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getOwner() {
        return owner;
    }

    public Player getOpponent() {
        return opponent;
    }

    public boolean hasOpponentEnteredThisRound() {
        return opponentEnteredThisRound;
    }

    public int getOwnerDiscardCountAtEntry() {
        return ownerDiscardCountAtEntry;
    }

    public int getAttackDamageToDefense() {
        return attackDamageToDefense;
    }
}
