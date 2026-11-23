package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.CardSchool;

public class DisciplineEffect implements CardEffectHandler {

    private static final int DEFENSE_BONUS = 1;

    @Override
    public void onEntry(EffectContext context) {

        // Only cards destroyed on the battlefield count.
        CardSchool lastDestroyedSchool =
                context.getOwner().getLastDestroyedSchool();

        if (lastDestroyedSchool != CardSchool.GUERRIERI) {
            return;
        }

        // Increase both maximum and current defense.
        context.getSourceCard().increaseMaxDefense(
                DEFENSE_BONUS);
    }
}
