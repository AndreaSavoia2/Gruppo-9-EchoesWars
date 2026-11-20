package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.Card;

public class HealingEffect implements CardEffectHandler {

    private static final int HEAL_PER_ROUND = 1;
    private static final int MAX_TOTAL_HEALING = 3;

    @Override
    public void onRoundEnd(EffectContext context) {

        Card card = context.getSourceCard();

        // Healing requires a surviving card on the battlefield.
        if (card.isDestroyed()) {
            return;
        }

        // Each card can restore at most three actual HP.
        if (card.getHealingDone() >= MAX_TOTAL_HEALING) {
            return;
        }

        // Player.heal() enforces the maximum of 20 HP
        // and does not revive defeated players.
        int actualHealing = context.getOwner().heal(HEAL_PER_ROUND);

        // Only HP actually restored count toward the limit.
        card.addHealingDone(actualHealing);
    }
}
