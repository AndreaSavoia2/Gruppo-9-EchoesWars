package com.echoeswars.game;

import java.util.ArrayList;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.combat.CombatResult;
import com.echoeswars.combat.PlayerLife;

import com.echoeswars.effects.EffectResolver;

import com.echoeswars.model.Card;
import com.echoeswars.model.Player;

import com.echoeswars.replay.ReplayEvent;
import com.echoeswars.replay.ReplayRound;

import com.echoeswars.replay.events.CardsDrawnEvent;
import com.echoeswars.replay.events.CardsPlayedEvent;
import com.echoeswars.replay.events.CombatResolvedEvent;
import com.echoeswars.replay.events.DrawnCard;
import com.echoeswars.replay.events.PlayedCard;
import com.echoeswars.replay.events.RoundEndedEvent;
import com.echoeswars.replay.events.CardDestroyedEvent;
import com.echoeswars.replay.events.CardReplacedEvent;
import com.echoeswars.replay.events.CardRoundAdvancedEvent;

public class RoundManager {

        private final CombatResolver combatResolver;
        private final EffectResolver effectResolver;

        // Cards replaced at the end of a round enter
        // the battlefield at the beginning of the next round.
        private final ArrayList<String> pendingEntryPlayerIds;

        public RoundManager(
                        CombatResolver combatResolver,
                        EffectResolver effectResolver) {

                if (combatResolver == null || effectResolver == null) {
                        throw new IllegalArgumentException(
                                        "RoundManager dependencies cannot be null.");
                }

                this.combatResolver = combatResolver;
                this.effectResolver = effectResolver;
                this.pendingEntryPlayerIds = new ArrayList<>();
        }

        /**
         * Plays one complete round and returns its replay record.
         */

        public ReplayRound playRound(
                        int round,
                        Player player1,
                        Player player2) {

                if (round < 1) {
                        throw new IllegalArgumentException(
                                        "Round number must be positive.");
                }

                ReplayRound replayRound = new ReplayRound(round);

                // Initial cards were drawn before the match started.
                // Their draw events are recorded in round one.
                if (round == 1) {

                        ArrayList<DrawnCard> drawnCards = new ArrayList<>();

                        addDrawnCard(drawnCards, player1);
                        addDrawnCard(drawnCards, player2);

                        if (!drawnCards.isEmpty()) {
                                replayRound.addEvent(
                                                new CardsDrawnEvent(drawnCards));
                        }
                }

                // Determine which cards enter the battlefield
                // for the first time in this round.
                boolean player1Entered = round == 1
                                || pendingEntryPlayerIds.contains(
                                                player1.getPlayerId());

                boolean player2Entered = round == 1
                                || pendingEntryPlayerIds.contains(
                                                player2.getPlayerId());

                // Entry information is consumed once per round.
                pendingEntryPlayerIds.clear();

                // Resolve and record entry effects.
                ArrayList<ReplayEvent> entryEvents = new ArrayList<>();

                effectResolver.resolveEntryEffects(
                                player1,
                                player2,
                                player1Entered,
                                player2Entered,
                                entryEvents);

                addEffectEvents(replayRound, entryEvents);

                // Update and record battlefield duration.
                advanceCardRound(player1, replayRound);
                advanceCardRound(player2, replayRound);

                // Resolve and record beginning-of-round effects.
                ArrayList<ReplayEvent> startEvents = new ArrayList<>();

                effectResolver.resolveRoundStartEffects(
                                player1,
                                player2,
                                startEvents);

                addEffectEvents(replayRound, startEvents);

                // Record the cards participating in combat.
                ArrayList<PlayedCard> playedCards = new ArrayList<>();

                addPlayedCard(playedCards, player1);
                addPlayedCard(playedCards, player2);

                if (!playedCards.isEmpty()) {
                        replayRound.addEvent(
                                        new CardsPlayedEvent(playedCards));
                }

                // Resolve simultaneous combat.
                CombatResult combatResult = combatResolver.resolve(
                                player1,
                                player2);

                ReplayEvent combatEvent = new CombatResolvedEvent(combatResult);

                replayRound.addEvent(combatEvent);

                // HP defeat takes priority over post-combat effects,
                // healing, and mandatory card replacements.
                if (player1.getHealth() <= 0
                                || player2.getHealth() <= 0) {

                        PlayerLife playersLife = new PlayerLife(
                                        player1.getHealth(),
                                        player2.getHealth());

                        replayRound.addEvent(
                                        new RoundEndedEvent(playersLife));

                        return replayRound;
                }

                // Calculate DEF lost during ordinary attack resolution.
                // Only ordinary attack damage counts.
                // Reflection damage is excluded.
                int player1AttackDamageToDefense = combatResult.getDamageToFirstCardDefense();

                int player2AttackDamageToDefense = combatResult.getDamageToSecondCardDefense();

                // Resolve and record post-combat effects.
                ArrayList<ReplayEvent> postCombatEvents = new ArrayList<>();

                effectResolver.resolveAfterCombatEffects(
                                player1,
                                player2,
                                player1AttackDamageToDefense,
                                player2AttackDamageToDefense,
                                combatResult.isFirstAttackDestroyedOpponent(),
                                combatResult.isSecondAttackDestroyedOpponent(),
                                postCombatEvents);

                addEffectEvents(replayRound, postCombatEvents);

                // Resolve and record end-of-round effects.
                ArrayList<ReplayEvent> roundEndEvents = new ArrayList<>();

                effectResolver.resolveRoundEndEffects(
                                player1,
                                player2,
                                player1Entered,
                                player2Entered,
                                roundEndEvents);

                addEffectEvents(replayRound, roundEndEvents);

                // Resolve and record destruction effects.
                ArrayList<ReplayEvent> destructionEvents = new ArrayList<>();

                effectResolver.resolveDestructionEffects(
                                player1,
                                player2,
                                destructionEvents);

                addEffectEvents(replayRound, destructionEvents);

                // Register both destructions before replacing either card.
                registerDestroyedActiveCard(player1, replayRound);
                registerDestroyedActiveCard(player2, replayRound);

                // Replace destroyed cards and record the replacements.
                replaceDestroyedActiveCard(player1, replayRound);
                replaceDestroyedActiveCard(player2, replayRound);

                // Capture health at the end of the round.
                PlayerLife playersLife = new PlayerLife(
                                player1.getHealth(),
                                player2.getHealth());

                replayRound.addEvent(
                                new RoundEndedEvent(playersLife));

                return replayRound;
        }

        private void addDrawnCard(
                        ArrayList<DrawnCard> drawnCards,
                        Player player) {

                Card activeCard = player.getActiveCard();

                if (activeCard == null) {
                        return;
                }

                drawnCards.add(
                                new DrawnCard(
                                                player.getPlayerId(),
                                                activeCard.getCardId(),
                                                activeCard.getInstanceId()));
        }

        private void addPlayedCard(
                        ArrayList<PlayedCard> playedCards,
                        Player player) {

                Card activeCard = player.getActiveCard();

                if (activeCard == null || activeCard.isDestroyed()) {
                        return;
                }

                playedCards.add(
                                new PlayedCard(
                                                player.getPlayerId(),
                                                activeCard.getCardId(),
                                                activeCard.getInstanceId()));
        }

        private void registerDestroyedActiveCard(
                        Player player,
                        ReplayRound replayRound) {

                Card activeCard = player.getActiveCard();

                if (activeCard == null || !activeCard.isDestroyed()) {
                        return;
                }

                if (!player.getDiscardPile().contains(activeCard)) {

                        player.registerDestroyedCard(activeCard);

                        replayRound.addEvent(
                                        new CardDestroyedEvent(
                                                        player.getPlayerId(),
                                                        activeCard.getInstanceId(),
                                                        activeCard.getCardId()));
                }
        }

        private void addEffectEvents(
                        ReplayRound replayRound,
                        ArrayList<ReplayEvent> events) {

                for (ReplayEvent event : events) {
                        replayRound.addEvent(event);
                }
        }

        private void replaceDestroyedActiveCard(
                        Player player,
                        ReplayRound replayRound) {

                Card destroyedCard = player.getActiveCard();

                if (destroyedCard == null || !destroyedCard.isDestroyed()) {
                        return;
                }

                String destroyedInstanceId = destroyedCard.getInstanceId();

                player.replaceDestroyedCard();

                Card newCard = player.getActiveCard();

                replayRound.addEvent(
                                new CardReplacedEvent(
                                                player.getPlayerId(),
                                                destroyedInstanceId,
                                                newCard != null
                                                                ? newCard.getInstanceId()
                                                                : null,
                                                newCard != null
                                                                ? newCard.getCardId()
                                                                : null));

                // Schedule entry effects for the next round.
                if (newCard != null) {
                        pendingEntryPlayerIds.add(player.getPlayerId());
                }
        }

        private void advanceCardRound(
                        Player player,
                        ReplayRound replayRound) {

                Card activeCard = player.getActiveCard();

                if (activeCard == null || activeCard.isDestroyed()) {
                        return;
                }

                activeCard.incrementRoundsInPlay();

                replayRound.addEvent(
                                new CardRoundAdvancedEvent(
                                                player.getPlayerId(),
                                                activeCard.getInstanceId(),
                                                activeCard.getRoundsInPlay()));
        }

}
