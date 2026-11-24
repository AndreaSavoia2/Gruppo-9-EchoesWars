
package com.echoeswars.effects;

import com.echoeswars.effects.handlers.*;
import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.Player;
import com.echoeswars.replay.ReplayEvent;
import com.echoeswars.replay.events.EffectTriggeredEvent;
import com.echoeswars.replay.events.DiscardedCard;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class EffectResolver {

    private final Map<CardEffect, CardEffectHandler> handlers;

    public EffectResolver() {
        handlers = new EnumMap<>(CardEffect.class);

        handlers.put(CardEffect.SABOTAGGIO, new SabotageEffect());
        handlers.put(CardEffect.MALEDIZIONE, new CurseEffect());
        handlers.put(CardEffect.MEMORIA, new MemoryEffect());
        handlers.put(CardEffect.FURIA, new FuryEffect());
        handlers.put(CardEffect.DISCIPLINA, new DisciplineEffect());
        handlers.put(CardEffect.PREPARAZIONE_ARCANA, new ArcanePreparationEffect());
        handlers.put(CardEffect.GUARDIANO, new GuardianEffect());
        handlers.put(CardEffect.GUARIGIONE, new HealingEffect());
        handlers.put(CardEffect.IMBOSCATA, new AmbushEffect());
        handlers.put(CardEffect.ASSALTO, new AssaultEffect());
        handlers.put(CardEffect.BERSERKER, new BerserkerEffect());
        handlers.put(CardEffect.SACRIFICIO, new SacrificeEffect());
        handlers.put(CardEffect.DRENAGGIO, new DrainEffect());
        handlers.put(CardEffect.CONDANNA, new CondemnationEffect());
        handlers.put(CardEffect.ANALISI, new AnalysisEffect());
    }

    private void executeTracked(
            String phase,
            Card card,
            Player owner,
            Player opponent,
            EffectContext context,
            ArrayList<ReplayEvent> events,
            java.util.function.Consumer<EffectContext> action) {

        EffectStateSnapshot before = new EffectStateSnapshot(card, owner, opponent);

        // Capture the opponent's deck before the effect.
        List<Card> opponentDeckBefore = opponent.getDeck();

        // Capture the Analysis target before the effect.
        String analysisTargetBefore = card.getAnalysisTargetId();

        // Execute the effect exactly once.
        action.accept(context);

        EffectStateSnapshot after = new EffectStateSnapshot(card, owner, opponent);

        Map<String, Integer> changes = before.differences(after);

        // Capture the Analysis target after the effect.
        String analysisTargetAfter = card.getAnalysisTargetId();

        boolean analysisTargetChanged = !java.util.Objects.equals(
                analysisTargetBefore,
                analysisTargetAfter);

        // Capture the remaining card instance IDs.
        Set<String> remainingIds = new HashSet<>();

        for (Card remainingCard : opponent.getDeck()) {
            remainingIds.add(remainingCard.getInstanceId());
        }

        ArrayList<DiscardedCard> discardedCards = new ArrayList<>();

        for (Card previousCard : opponentDeckBefore) {

            if (!remainingIds.contains(
                    previousCard.getInstanceId())) {

                discardedCards.add(
                        new DiscardedCard(
                                previousCard.getInstanceId(),
                                previousCard.getCardId()));
            }
        }

        // Record every observable change.
        if (!changes.isEmpty()
                || !discardedCards.isEmpty()
                || analysisTargetChanged) {

            events.add(
                    new EffectTriggeredEvent(
                            phase,
                            card.getEffect().name(),
                            card.getInstanceId(),
                            card.getCardId(),
                            changes,
                            discardedCards,
                            analysisTargetChanged
                                    ? analysisTargetAfter
                                    : null));
        }
    }

    public void resolveEntryEffects(
            Player player1,
            Player player2,
            boolean player1Entered,
            boolean player2Entered) {

        resolveEntryEffects(
                player1, player2,
                player1Entered, player2Entered,
                new ArrayList<>());
    }

    public void resolveEntryEffects(
            Player player1,
            Player player2,
            boolean player1Entered,
            boolean player2Entered,
            ArrayList<ReplayEvent> events) {

        Card firstCard = player1Entered
                ? player1.getActiveCard()
                : null;

        Card secondCard = player2Entered
                ? player2.getActiveCard()
                : null;

        // Both entry effects use the same initial discard counts.
        int firstDiscardCount = player1.getDiscardPileSize();
        int secondDiscardCount = player2.getDiscardPileSize();

        resolveEntryEffect(
                firstCard, player1, player2,
                player2Entered, firstDiscardCount, events);

        resolveEntryEffect(
                secondCard, player2, player1,
                player1Entered, secondDiscardCount, events);
    }

    private void resolveEntryEffect(
            Card card,
            Player owner,
            Player opponent,
            boolean opponentEnteredThisRound,
            int ownerDiscardCountAtEntry,
            ArrayList<ReplayEvent> events) {

        if (card == null || card.isDestroyed()) {
            return;
        }

        CardEffectHandler handler = handlers.get(card.getEffect());

        if (handler == null) {
            return;
        }

        EffectContext context = new EffectContext(
                card, owner, opponent,
                opponentEnteredThisRound,
                ownerDiscardCountAtEntry);

        executeTracked(
                "ENTRY", card, owner, opponent,
                context, events, handler::onEntry);
    }

    public void resolveRoundStartEffects(
            Player player1,
            Player player2) {

        resolveRoundStartEffects(
                player1, player2, new ArrayList<>());
    }

    public void resolveRoundStartEffects(
            Player player1,
            Player player2,
            ArrayList<ReplayEvent> events) {

        resolveRoundStartEffect(player1, player2, events);
        resolveRoundStartEffect(player2, player1, events);
    }

    private void resolveRoundStartEffect(
            Player owner,
            Player opponent,
            ArrayList<ReplayEvent> events) {

        Card card = owner.getActiveCard();

        if (card == null || card.isDestroyed()) {
            return;
        }

        CardEffectHandler handler = handlers.get(card.getEffect());

        if (handler == null) {
            return;
        }

        EffectContext context = new EffectContext(card, owner, opponent);

        executeTracked(
                "ROUND_START", card, owner, opponent,
                context, events, handler::onRoundStart);
    }

    public void resolveAfterCombatEffects(
            Player player1,
            Player player2,
            int player1AttackDamageToDefense,
            int player2AttackDamageToDefense,
            boolean firstAttackDestroyedOpponent,
            boolean secondAttackDestroyedOpponent) {

        resolveAfterCombatEffects(
                player1, player2,
                player1AttackDamageToDefense,
                player2AttackDamageToDefense,
                firstAttackDestroyedOpponent,
                secondAttackDestroyedOpponent,
                new ArrayList<>());
    }

    public void resolveAfterCombatEffects(
            Player player1,
            Player player2,
            int player1AttackDamageToDefense,
            int player2AttackDamageToDefense,
            boolean firstAttackDestroyedOpponent,
            boolean secondAttackDestroyedOpponent,
            ArrayList<ReplayEvent> events) {

        resolveAfterCombatEffect(
                player1, player2,
                player1AttackDamageToDefense,
                firstAttackDestroyedOpponent, events);

        resolveAfterCombatEffect(
                player2, player1,
                player2AttackDamageToDefense,
                secondAttackDestroyedOpponent, events);
    }

    private void resolveAfterCombatEffect(
            Player owner,
            Player opponent,
            int attackDamageToDefense,
            boolean attackDestroyedOpponent,
            ArrayList<ReplayEvent> events) {

        Card card = owner.getActiveCard();

        if (card == null || card.isDestroyed()) {
            return;
        }

        CardEffectHandler handler = handlers.get(card.getEffect());

        if (handler == null) {
            return;
        }

        EffectContext context = new EffectContext(
                card, owner, opponent,
                false,
                owner.getDiscardPileSize(),
                attackDamageToDefense,
                attackDestroyedOpponent);

        executeTracked(
                "POST_COMBAT", card, owner, opponent,
                context, events, handler::onAfterCombat);
    }

    public void resolveRoundEndEffects(
            Player player1,
            Player player2,
            boolean player1Entered,
            boolean player2Entered) {

        resolveRoundEndEffects(
                player1, player2,
                player1Entered, player2Entered,
                new ArrayList<>());
    }

    public void resolveRoundEndEffects(
            Player player1,
            Player player2,
            boolean player1Entered,
            boolean player2Entered,
            ArrayList<ReplayEvent> events) {

        resolveRoundEndEffect(
                player1, player2, player1Entered, events);

        resolveRoundEndEffect(
                player2, player1, player2Entered, events);
    }

    private void resolveRoundEndEffect(
            Player owner,
            Player opponent,
            boolean enteredThisRound,
            ArrayList<ReplayEvent> events) {

        Card card = owner.getActiveCard();

        if (card == null || card.isDestroyed()) {
            return;
        }

        if (!enteredThisRound
                && (card.getEffect() == CardEffect.GUARDIANO
                        || card.getEffect() == CardEffect.IMBOSCATA)) {
            return;
        }

        CardEffectHandler handler = handlers.get(card.getEffect());

        if (handler == null) {
            return;
        }

        EffectContext context = new EffectContext(card, owner, opponent);

        executeTracked(
                "ROUND_END", card, owner, opponent,
                context, events, handler::onRoundEnd);
    }

    public void resolveDestructionEffects(
            Player player1,
            Player player2) {

        resolveDestructionEffects(
                player1, player2, new ArrayList<>());
    }

    public void resolveDestructionEffects(
            Player player1,
            Player player2,
            ArrayList<ReplayEvent> events) {

        resolveDestructionEffect(player1, player2, events);
        resolveDestructionEffect(player2, player1, events);
    }

    private void resolveDestructionEffect(
            Player owner,
            Player opponent,
            ArrayList<ReplayEvent> events) {

        Card card = owner.getActiveCard();

        if (card == null || !card.isDestroyed()) {
            return;
        }

        CardEffectHandler handler = handlers.get(card.getEffect());

        if (handler == null) {
            return;
        }

        EffectContext context = new EffectContext(card, owner, opponent);

        executeTracked(
                "DESTRUCTION", card, owner, opponent,
                context, events, handler::onDestroyed);
    }
}
