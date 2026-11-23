package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

public class SacrificeEffect implements CardEffectHandler {

    private static final int HEAL_AMOUNT = 2;

    @Override
    public void onDestroyed(EffectContext context) {

        // Restore HP when the source card is destroyed.
        // Player.heal() enforces the maximum of 20 HP
        // and does not revive defeated players.
        context.getOwner().heal(HEAL_AMOUNT);
    }
}
