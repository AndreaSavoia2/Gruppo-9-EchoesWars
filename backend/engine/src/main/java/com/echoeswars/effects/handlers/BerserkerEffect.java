package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.Card;

import java.util.IdentityHashMap;
import java.util.Map;

public class BerserkerEffect implements CardEffectHandler {

    private static final int DEFENSE_COST = 1;
    private static final int ATTACK_GAIN = 2;
    private static final int MAX_BERSERKER_BONUS = 4;

    // Track only the attack gained from Berserker.
    private final Map<Card, Integer> berserkerBonuses =
            new IdentityHashMap<>();

    @Override
    public void onRoundStart(EffectContext context) {

        Card card = context.getSourceCard();

        // Berserker starts activating from its second round.
        if (card.getRoundsInPlay() < 2) {
            return;
        }

        int currentBonus =
                berserkerBonuses.getOrDefault(card, 0);

        // Stop after reaching the maximum Berserker bonus.
        if (currentBonus >= MAX_BERSERKER_BONUS) {
            return;
        }

        // The card must remain at or above 1 DEF.
        if (card.getCurrentDefense() <= DEFENSE_COST) {
            return;
        }

        // Pay the DEF cost.
        card.reduceCurrentDefense(DEFENSE_COST);

        // Gain the corresponding permanent attack bonus.
        card.addAttackBonus(ATTACK_GAIN);

        berserkerBonuses.put(
                card,
                currentBonus + ATTACK_GAIN);
    }
}
