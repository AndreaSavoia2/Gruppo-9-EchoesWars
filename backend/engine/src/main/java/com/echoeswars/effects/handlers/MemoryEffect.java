package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;

public class MemoryEffect implements CardEffectHandler {

    private static final int CARDS_PER_BONUS = 5;
    private static final int MAX_ATTACK_BONUS = 3;

    @Override
    public void onEntry(EffectContext context) {

        // Use the discard count captured before entry effects.
        int discardedCards =
                context.getOwnerDiscardCountAtEntry();

        int attackBonus = Math.min(
                MAX_ATTACK_BONUS,
                discardedCards / CARDS_PER_BONUS);

        // Apply the entry bonus to the source card.
        context.getSourceCard().addAttackBonus(
                attackBonus);
    }
}
