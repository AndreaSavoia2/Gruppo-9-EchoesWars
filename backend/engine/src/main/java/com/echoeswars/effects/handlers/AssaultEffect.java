package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.Card;

public class AssaultEffect implements CardEffectHandler {

    private static final int MAX_ASSAULT_BONUS = 3;

    @Override
    public void onRoundStart(EffectContext context) {

        Card card = context.getSourceCard();

        int roundsInPlay = card.getRoundsInPlay();

        // Assault does not activate during the first round.
        if (roundsInPlay <= 1) {
            return;
        }

        // Rounds 2, 3 and 4 grant one permanent attack each.
        if (roundsInPlay <= MAX_ASSAULT_BONUS + 1) {
            card.addAttackBonus(1);
        }
    }
}
