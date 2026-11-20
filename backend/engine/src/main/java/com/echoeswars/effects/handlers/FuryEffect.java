package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.Card;

import java.util.IdentityHashMap;
import java.util.Map;

public class FuryEffect implements CardEffectHandler {

    private static final int MAX_FURY_BONUS = 2;

    private final Map<Card, Integer> furyBonuses =
            new IdentityHashMap<>();

    @Override
    public void onAfterCombat(EffectContext context) {

        Card card = context.getSourceCard();

        // Fury activates only after attack damage to DEF.
        if (context.getAttackDamageToDefense() <= 0) {
            return;
        }

        // Destroyed cards cannot gain Fury.
        if (card.isDestroyed()) {
            return;
        }

        int currentBonus = furyBonuses.getOrDefault(card, 0);

        if (currentBonus >= MAX_FURY_BONUS) {
            return;
        }

        // This bonus applies only to future combats.
        card.addAttackBonus(1);
        furyBonuses.put(card, currentBonus + 1);
    }
}
