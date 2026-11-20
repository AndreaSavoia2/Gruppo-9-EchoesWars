package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

import com.echoeswars.model.Card;

public class CurseEffect implements CardEffectHandler {

    private static final int DEFENSE_REDUCTION = 2;

    @Override
    public void onEntry(EffectContext context) {

        // Curse does not affect cards entering simultaneously.
        if (context.hasOpponentEnteredThisRound()) {
            return;
        }

        Card opponentCard =
                context.getOpponent().getActiveCard();

        // There must be a surviving card already in play.
        if (opponentCard == null || opponentCard.isDestroyed()) {
            return;
        }

        // Reduce current defense, never below one.
        opponentCard.reduceCurrentDefense(
                DEFENSE_REDUCTION);
    }
}
