
package com.echoeswars.effects;

import com.echoeswars.model.Card;
import com.echoeswars.model.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class EffectStateSnapshot {

    private final Map<String, Integer> values =
            new LinkedHashMap<>();

    public EffectStateSnapshot(
            Card source,
            Player owner,
            Player opponent) {

        captureCard("source", source);
        captureCard("target", opponent.getActiveCard());

        values.put("ownerHealth", owner.getHealth());
        values.put("opponentHealth", opponent.getHealth());

        values.put("ownerDeckSize", owner.getDeck().size());
        values.put("opponentDeckSize", opponent.getDeck().size());

        values.put("ownerDiscardSize", owner.getDiscardPileSize());
        values.put("opponentDiscardSize", opponent.getDiscardPileSize());
    }

    private void captureCard(String prefix, Card card) {

        if (card == null) {
            return;
        }

        values.put(prefix + "AttackBonus",
                card.getAttackBonus());

        values.put(prefix + "TemporaryAttackBonus",
                card.getTemporaryAttackBonus());

        values.put(prefix + "Defense",
                card.getCurrentDefense());

        values.put(prefix + "MaxDefense",
                card.getMaxDefense());

        values.put(prefix + "Protection",
                card.getProtection());

        values.put(prefix + "HealingDone",
                card.getHealingDone());
    }

    public Map<String, Integer> differences(
            EffectStateSnapshot after) {

        Map<String, Integer> changes =
                new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry
                : values.entrySet()) {

            String key = entry.getKey();
            Integer before = entry.getValue();
            Integer afterValue = after.values.get(key);

            if (afterValue != null
                    && !before.equals(afterValue)) {

                changes.put(key + "Before", before);
                changes.put(key + "After", afterValue);
            }
        }

        return changes;
    }
}
