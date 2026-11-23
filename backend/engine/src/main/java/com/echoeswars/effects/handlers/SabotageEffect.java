package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

public class SabotageEffect implements CardEffectHandler {

    @Override
    public void onEntry(EffectContext context) {

        // Discard the top card of the opponent's deck.
        // An empty deck does not cause defeat here.
        context.getOpponent().discardTopCard();
    }
}
