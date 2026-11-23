
package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

public class GuardianEffect implements CardEffectHandler {

    private static final int INITIAL_PROTECTION = 2;

    @Override
    public void onEntry(EffectContext context) {

        // Grant protection during the first round in play.
        context.getSourceCard().addProtection(
                INITIAL_PROTECTION);
    }

    @Override
    public void onRoundEnd(EffectContext context) {

        // Unused protection expires after the entry round.
        context.getSourceCard().clearProtection();
    }
}
