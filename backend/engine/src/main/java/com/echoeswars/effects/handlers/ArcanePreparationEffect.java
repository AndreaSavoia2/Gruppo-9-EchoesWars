package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.CardSchool;

public class ArcanePreparationEffect implements CardEffectHandler {

    private static final int ATTACK_BONUS = 1;

    @Override
    public void onEntry(EffectContext context) {

        // Only cards destroyed on the battlefield count.
        CardSchool lastDestroyedSchool =
                context.getOwner().getLastDestroyedSchool();

        if (lastDestroyedSchool != CardSchool.MAGHI) {
            return;
        }

        // The attack bonus remains active while the card is in play.
        context.getSourceCard().addAttackBonus(ATTACK_BONUS);
    }
}
