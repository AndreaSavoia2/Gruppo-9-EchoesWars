package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

public class AmbushEffect implements CardEffectHandler {

    private static final int FIRST_ROUND_ATTACK_BONUS = 2;

    @Override
    public void onEntry(EffectContext context) {

        // Ambush applies only when the card enters the field.
        context.getSourceCard().addTemporaryAttackBonus(
                FIRST_ROUND_ATTACK_BONUS);
    }

    @Override
    public void onRoundEnd(EffectContext context) {

        // The bonus expires after the entry round.
        context.getSourceCard().clearTemporaryAttackBonus();
    }
}
