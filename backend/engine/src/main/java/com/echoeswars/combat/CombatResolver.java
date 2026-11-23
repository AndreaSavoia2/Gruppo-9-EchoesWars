
package com.echoeswars.combat;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class CombatResolver {

        public CombatResult resolve(
                        Player firstPlayer,
                        Player secondPlayer) {

                Card firstCard = firstPlayer.getActiveCard();
                Card secondCard = secondPlayer.getActiveCard();

                if (firstCard == null || secondCard == null) {
                        throw new IllegalStateException(
                                        "Combat cannot start: missing active card.");
                }

                // Calculate all attack bonuses before applying damage.
                int firstEchoBonus = EchoResolver.calculateEchoBonus(
                                firstCard, secondCard);

                int secondEchoBonus = EchoResolver.calculateEchoBonus(
                                secondCard, firstCard);

                int firstDuelistBonus = calculateDuelistBonus(
                                firstCard, secondCard);

                int secondDuelistBonus = calculateDuelistBonus(
                                secondCard, firstCard);

                int firstOpportunistBonus = calculateOpportunistBonus(
                                firstCard, secondCard);

                int secondOpportunistBonus = calculateOpportunistBonus(
                                secondCard, firstCard);

                int firstAttack = firstCard.getBaseEffectiveAttack()
                                + firstEchoBonus
                                + firstDuelistBonus
                                + firstOpportunistBonus;

                int secondAttack = secondCard.getBaseEffectiveAttack()
                                + secondEchoBonus
                                + secondDuelistBonus
                                + secondOpportunistBonus;

                // Capture the state before ordinary attacks.
                int firstDefenseBefore = firstCard.getCurrentDefense();
                int secondDefenseBefore = secondCard.getCurrentDefense();

                int firstProtectionBefore = firstCard.getProtection();
                int secondProtectionBefore = secondCard.getProtection();

                // Apply Resistance and Piercing.
                int damageToFirstCard = applyResistance(
                                firstCard, secondCard, secondAttack);

                int damageToSecondCard = applyResistance(
                                secondCard, firstCard, firstAttack);

                int firstResistanceReduction = secondAttack - damageToFirstCard;

                int secondResistanceReduction = firstAttack - damageToSecondCard;

                boolean firstPiercingBypassedResistance = bypassesActiveResistance(firstCard, secondCard);

                boolean secondPiercingBypassedResistance = bypassesActiveResistance(secondCard, firstCard);

                // Resolve both ordinary attacks.
                firstCard.takeDamage(damageToFirstCard);
                secondCard.takeDamage(damageToSecondCard);

                // Capture the state before Reflection.
                int firstDefenseAfterAttack = firstCard.getCurrentDefense();
                int secondDefenseAfterAttack = secondCard.getCurrentDefense();

                int firstProtectionAfterAttack = firstCard.getProtection();
                int secondProtectionAfterAttack = secondCard.getProtection();

                // Ordinary attack kills must be recorded before Reflection.
                boolean firstAttackDestroyedOpponent = secondCard.isDestroyed();

                boolean secondAttackDestroyedOpponent = firstCard.isDestroyed();

                // Reflection triggers on incoming ordinary attack damage,
                // even if Protection absorbed all of it.
                boolean firstReflectionTriggered = firstCard.getEffect() == CardEffect.RIFLESSO
                                && damageToFirstCard > 0;

                boolean secondReflectionTriggered = secondCard.getEffect() == CardEffect.RIFLESSO
                                && damageToSecondCard > 0;

                if (firstReflectionTriggered) {
                        secondCard.takeDamage(1);
                }

                if (secondReflectionTriggered) {
                        firstCard.takeDamage(1);
                }

                int firstDefenseAfter = firstCard.getCurrentDefense();
                int secondDefenseAfter = secondCard.getCurrentDefense();

                // Capture Protection after ordinary attacks and Reflection.
                int firstProtectionAfter = firstCard.getProtection();
                int secondProtectionAfter = secondCard.getProtection();

                // Overflow depends on the final survival state,
                // but its amount comes only from ordinary attacks.
                int firstPlayerDamage = 0;
                int secondPlayerDamage = 0;

                if (!firstCard.isDestroyed() && secondCard.isDestroyed()) {
                        secondPlayerDamage = Math.max(
                                        0, -secondDefenseAfterAttack);

                        secondPlayer.takeDamage(secondPlayerDamage);
                }

                if (!secondCard.isDestroyed() && firstCard.isDestroyed()) {
                        firstPlayerDamage = Math.max(
                                        0, -firstDefenseAfterAttack);

                        firstPlayer.takeDamage(firstPlayerDamage);
                }

                // Build the attack breakdown for each card.
                Map<String, Integer> firstAttackBreakdown = createAttackBreakdown(
                                firstCard,
                                firstEchoBonus,
                                firstDuelistBonus,
                                firstOpportunistBonus);

                Map<String, Integer> secondAttackBreakdown = createAttackBreakdown(
                                secondCard,
                                secondEchoBonus,
                                secondDuelistBonus,
                                secondOpportunistBonus);

                // Each damage breakdown describes damage dealt
                // by the source card to the opposing card.
                Map<String, Integer> firstDamageBreakdown = createDamageBreakdown(
                                firstAttack,
                                secondResistanceReduction,
                                damageToSecondCard,
                                secondProtectionBefore
                                                - secondProtectionAfterAttack,
                                secondDefenseBefore,
                                secondDefenseAfterAttack,
                                secondPlayerDamage);

                Map<String, Integer> secondDamageBreakdown = createDamageBreakdown(
                                secondAttack,
                                firstResistanceReduction,
                                damageToFirstCard,
                                firstProtectionBefore
                                                - firstProtectionAfterAttack,
                                firstDefenseBefore,
                                firstDefenseAfterAttack,
                                firstPlayerDamage);

                CombatCardResult firstCardResult = new CombatCardResult(
                                firstCard.getInstanceId(),
                                firstCard.getCardId(),
                                firstAttack,
                                firstDefenseBefore,
                                firstDefenseAfter,
                                firstCard.isDestroyed(),
                                firstAttackBreakdown,
                                firstDamageBreakdown,
                                firstReflectionTriggered ? 1 : 0,
                                firstPiercingBypassedResistance,
                                firstProtectionBefore,
                                firstProtectionAfter);

                CombatCardResult secondCardResult = new CombatCardResult(
                                secondCard.getInstanceId(),
                                secondCard.getCardId(),
                                secondAttack,
                                secondDefenseBefore,
                                secondDefenseAfter,
                                secondCard.isDestroyed(),
                                secondAttackBreakdown,
                                secondDamageBreakdown,
                                secondReflectionTriggered ? 1 : 0,
                                secondPiercingBypassedResistance,
                                secondProtectionBefore,
                                secondProtectionAfter);

                return new CombatResult(
                                firstCardResult,
                                secondCardResult,
                                firstPlayerDamage,
                                secondPlayerDamage,
                                firstAttackDestroyedOpponent,
                                secondAttackDestroyedOpponent);
        }

        private Map<String, Integer> createAttackBreakdown(
                        Card card,
                        int echoBonus,
                        int duelistBonus,
                        int opportunistBonus) {

                Map<String, Integer> breakdown = new LinkedHashMap<>();

                breakdown.put("base", card.getAttack());
                breakdown.put("permanentBonus", card.getAttackBonus());
                breakdown.put("temporaryBonus",
                                card.getTemporaryAttackBonus());
                breakdown.put("echoBonus", echoBonus);
                breakdown.put("duelistBonus", duelistBonus);
                breakdown.put("opportunistBonus", opportunistBonus);

                return breakdown;
        }

        private Map<String, Integer> createDamageBreakdown(
                        int rawDamage,
                        int resistanceReduction,
                        int effectiveDamage,
                        int protectionAbsorbed,
                        int defenseBefore,
                        int defenseAfterOrdinaryAttack,
                        int overflowDamage) {

                Map<String, Integer> breakdown = new LinkedHashMap<>();

                int defenseDamage = Math.min(
                                Math.max(0, defenseBefore),
                                Math.max(0,
                                                defenseBefore - defenseAfterOrdinaryAttack));

                breakdown.put("rawDamage", rawDamage);
                breakdown.put("resistanceReduction",
                                resistanceReduction);
                breakdown.put("effectiveDamage", effectiveDamage);
                breakdown.put("protectionAbsorbed",
                                protectionAbsorbed);
                breakdown.put("defenseDamage", defenseDamage);
                breakdown.put("overflowDamage", overflowDamage);

                return breakdown;
        }

        private int applyResistance(
                        Card defendingCard,
                        Card attackingCard,
                        int incomingDamage) {

                if (attackingCard.getEffect() == CardEffect.PERFORANTE) {
                        return incomingDamage;
                }

                if (defendingCard.getEffect() != CardEffect.RESISTENZA) {
                        return incomingDamage;
                }

                int roundsInPlay = defendingCard.getRoundsInPlay();

                if (roundsInPlay < 1 || roundsInPlay > 2) {
                        return incomingDamage;
                }

                return Math.max(1, incomingDamage - 1);
        }

        private boolean bypassesActiveResistance(
                        Card attackingCard,
                        Card defendingCard) {

                return attackingCard.getEffect() == CardEffect.PERFORANTE
                                && defendingCard.getEffect() == CardEffect.RESISTENZA
                                && defendingCard.getRoundsInPlay() >= 1
                                && defendingCard.getRoundsInPlay() <= 2;
        }

        private int calculateDuelistBonus(
                        Card attackingCard,
                        Card defendingCard) {

                if (attackingCard.getEffect() != CardEffect.DUELLANTE) {
                        return 0;
                }

                return defendingCard.getSchool() == CardSchool.MAGHI
                                ? 1
                                : 0;
        }

        private int calculateOpportunistBonus(
                        Card attackingCard,
                        Card defendingCard) {

                if (attackingCard.getEffect() != CardEffect.OPPORTUNISTA) {
                        return 0;
                }

                return defendingCard.getCurrentDefense() < defendingCard.getMaxDefense()
                                ? 1
                                : 0;
        }
}
