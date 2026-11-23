package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.Card;

public class AnalysisEffect implements CardEffectHandler {

    @Override
    public void onRoundStart(EffectContext context) {

        Card source = context.getSourceCard();
        Card opponent = context.getOpponent().getActiveCard();

        if (source.isDestroyed() || opponent == null
                || opponent.isDestroyed()) {
            return;
        }

        String opponentId = opponent.getInstanceId();

        // Do not activate twice against the same opponent.
        if (opponentId.equals(source.getAnalysisTargetId())) {
            return;
        }

        // Remove any protection left from the previous encounter.
        source.clearProtection();

        // Remember the current opponent even if Analysis
        // does not grant protection.
        source.setAnalysisTargetId(opponentId);

        // Compare printed base attack values only.
        if (opponent.getAttack() > source.getAttack()) {
            source.addProtection(1);
        }
    }

    @Override
    public void onAfterCombat(EffectContext context) {

        Card opponent = context.getOpponent().getActiveCard();

        // Protection expires when the current encounter ends.
        if (opponent != null && opponent.isDestroyed()) {
            context.getSourceCard().clearProtection();
        }
    }
}
