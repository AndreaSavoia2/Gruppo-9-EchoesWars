
package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

public class CondemnationEffect implements CardEffectHandler {

    @Override
    public void onDestroyed(EffectContext context) {

        // Discard the top card of the opponent's deck
        // when the source card is destroyed on the battlefield.
        context.getOpponent().discardTopCard();
    }
}
