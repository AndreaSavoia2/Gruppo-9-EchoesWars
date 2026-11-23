package com.echoeswars.effects.handlers;

import com.echoeswars.effects.CardEffectHandler;
import com.echoeswars.effects.EffectContext;
import com.echoeswars.model.Card;

public class DrainEffect implements CardEffectHandler {

    @Override
    public void onAfterCombat(EffectContext context) {

        Card card = context.getSourceCard();

        // Drain requires the source card to survive.
        if (card.isDestroyed()) {
            return;
        }

        // Only a kill caused by this card's ordinary
        // attack can trigger Drain.
        if (!context.hasAttackDestroyedOpponent()) {
            return;
        }

        // Discard the top card of the opponent's deck.
        // An empty deck is handled by discardTopCard().
        context.getOpponent().discardTopCard();
    }
}
