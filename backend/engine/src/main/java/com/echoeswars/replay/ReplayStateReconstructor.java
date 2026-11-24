
package com.echoeswars.replay;

import com.echoeswars.combat.CombatCardResult;
import com.echoeswars.replay.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplayStateReconstructor {

    private void applyEffect(EffectTriggeredEvent event) {

        MutablePlayer owner = null;
        MutablePlayer opponent = null;

        // Identify the owner using the unique card instance ID.
        for (int i = 0; i < players.length; i++) {
            MutableCard active = players[i].activeCard;

            if (active != null
                    && active.instanceId.equals(
                            event.getSourceInstanceId())) {

                owner = players[i];
                opponent = players[1 - i];
                break;
            }
        }

        if (owner == null) {
            throw new IllegalStateException(
                    "Effect source not found: "
                            + event.getSourceInstanceId());
        }

        MutableCard source = owner.activeCard;
        MutableCard target = opponent.activeCard;

        if (!source.cardId.equals(event.getSourceCardId())) {
            throw new IllegalStateException(
                    "Effect source card ID mismatch");
        }

        if (!source.effect.equals(event.getEffect())) {
            throw new IllegalStateException(
                    "Effect type mismatch");
        }

        // Verify that every Before value has a corresponding After value.
        for (String key : event.getChanges().keySet()) {

            if (key.endsWith("Before")) {

                String afterKey = key.substring(
                        0,
                        key.length() - "Before".length())
                        + "After";

                if (!event.getChanges().containsKey(afterKey)) {
                    throw new IllegalStateException(
                            "Missing After value for " + key);
                }
            }
        }

        // Apply numeric changes recorded by EffectStateSnapshot.
        for (Map.Entry<String, Integer> entry : event.getChanges().entrySet()) {

            String key = entry.getKey();

            // Only After values modify the reconstructed state.
            if (!key.endsWith("After")) {
                continue;
            }

            String field = key.substring(
                    0, key.length() - "After".length());

            Integer before = event.getChanges().get(
                    field + "Before");

            if (before == null) {
                throw new IllegalStateException(
                        "Missing Before value for " + field);
            }

            applyEffectChange(
                    field,
                    before,
                    entry.getValue(),
                    source,
                    target,
                    owner,
                    opponent);
        }

        // Apply exact card identities removed from the opponent's deck.
        for (DiscardedCard discarded : event.getDiscardedCards()) {

            if (opponent.deck.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot discard from an empty deck");
            }

            MutableCard topCard = opponent.deck.get(0);

            if (!topCard.instanceId.equals(
                    discarded.getInstanceId())
                    || !topCard.cardId.equals(
                            discarded.getCardId())) {

                throw new IllegalStateException(
                        "Discarded card does not match deck order: "
                                + discarded.getInstanceId());
            }

            opponent.deck.remove(0);
            opponent.discardPile.add(topCard);
        }

        // Record the Analysis target, when present.
        if (event.getAnalysisTargetIdAfter() != null) {
            source.analysisTargetId = event.getAnalysisTargetIdAfter();
        }

        // Verify deck and discard sizes after applying the effect.
        verifyEffectCollectionSizes(
                event.getChanges(),
                owner,
                opponent);
    }

    private void applyEffectChange(
            String field,
            int before,
            int after,
            MutableCard source,
            MutableCard target,
            MutablePlayer owner,
            MutablePlayer opponent) {

        switch (field) {

            case "sourceAttackBonus":
                requireValue(field, source.attackBonus, before);
                source.attackBonus = after;
                break;

            case "sourceTemporaryAttackBonus":
                requireValue(
                        field, source.temporaryAttackBonus, before);
                source.temporaryAttackBonus = after;
                break;

            case "sourceDefense":
                requireValue(field, source.currentDefense, before);
                source.currentDefense = after;
                break;

            case "sourceMaxDefense":
                requireValue(field, source.maxDefense, before);
                source.maxDefense = after;
                break;

            case "sourceProtection":
                requireValue(field, source.protection, before);
                source.protection = after;
                break;

            case "sourceHealingDone":
                requireValue(field, source.healingDone, before);
                source.healingDone = after;
                break;

            case "targetAttackBonus":
                requireTarget(target, field);
                requireValue(field, target.attackBonus, before);
                target.attackBonus = after;
                break;

            case "targetTemporaryAttackBonus":
                requireTarget(target, field);
                requireValue(
                        field, target.temporaryAttackBonus, before);
                target.temporaryAttackBonus = after;
                break;

            case "targetDefense":
                requireTarget(target, field);
                requireValue(field, target.currentDefense, before);
                target.currentDefense = after;
                break;

            case "targetMaxDefense":
                requireTarget(target, field);
                requireValue(field, target.maxDefense, before);
                target.maxDefense = after;
                break;

            case "targetProtection":
                requireTarget(target, field);
                requireValue(field, target.protection, before);
                target.protection = after;
                break;

            case "targetHealingDone":
                requireTarget(target, field);
                requireValue(field, target.healingDone, before);
                target.healingDone = after;
                break;

            case "ownerHealth":
                requireValue(field, owner.health, before);
                owner.health = after;
                break;

            case "opponentHealth":
                requireValue(field, opponent.health, before);
                opponent.health = after;
                break;

            // Deck and discard sizes are checked after card movements.
            case "ownerDeckSize":
            case "opponentDeckSize":
            case "ownerDiscardSize":
            case "opponentDiscardSize":
                break;

            default:
                throw new IllegalStateException(
                        "Unsupported effect state field: " + field);
        }
    }

    private void verifyEffectCollectionSizes(
            Map<String, Integer> changes,
            MutablePlayer owner,
            MutablePlayer opponent) {

        verifyCollectionSize(
                changes,
                "ownerDeckSizeAfter",
                owner.deck.size());

        verifyCollectionSize(
                changes,
                "opponentDeckSizeAfter",
                opponent.deck.size());

        verifyCollectionSize(
                changes,
                "ownerDiscardSizeAfter",
                owner.discardPile.size());

        verifyCollectionSize(
                changes,
                "opponentDiscardSizeAfter",
                opponent.discardPile.size());
    }

    private void verifyCollectionSize(
            Map<String, Integer> changes,
            String key,
            int actual) {

        Integer expected = changes.get(key);

        if (expected != null && expected != actual) {
            throw new IllegalStateException(
                    "Replay collection size mismatch for "
                            + key
                            + ": expected " + expected
                            + ", actual " + actual);
        }
    }

    private void requireValue(
            String field,
            int actual,
            int expected) {

        if (actual != expected) {
            throw new IllegalStateException(
                    "Replay state mismatch for " + field
                            + ": expected " + expected
                            + ", actual " + actual);
        }
    }

    private void requireTarget(
            MutableCard target,
            String field) {

        if (target == null) {
            throw new IllegalStateException(
                    "Missing target card for " + field);
        }
    }

    private final MutablePlayer[] players = new MutablePlayer[2];

    public ReplayStateReconstructor(ReplayGameState initialState) {

        if (initialState == null
                || initialState.getPlayers().size() != 2) {
            throw new IllegalArgumentException(
                    "A replay requires exactly two players");
        }

        players[0] = new MutablePlayer(
                initialState.getPlayers().get(0));

        players[1] = new MutablePlayer(
                initialState.getPlayers().get(1));
    }

    public ReplayGameState applyRound(ReplayRound round) {

        for (ReplayEvent event : round.getEvents()) {
            applyEvent(event);
        }

        return snapshot();
    }

    public ReplayGameState snapshot() {

        return new ReplayGameState(List.of(
                players[0].snapshot(),
                players[1].snapshot()));
    }

    private void applyEvent(ReplayEvent event) {

        if (event instanceof CardsDrawnEvent) {
            verifyInitialDraw((CardsDrawnEvent) event);

        } else if (event instanceof CardRoundAdvancedEvent) {
            applyCardRoundAdvanced(
                    (CardRoundAdvancedEvent) event);

        } else if (event instanceof CombatResolvedEvent) {
            applyCombat((CombatResolvedEvent) event);

        } else if (event instanceof CardDestroyedEvent) {
            applyDestruction((CardDestroyedEvent) event);

        } else if (event instanceof CardReplacedEvent) {
            applyReplacement((CardReplacedEvent) event);

        } else if (event instanceof RoundEndedEvent) {
            verifyRoundEnd((RoundEndedEvent) event);

        } else if (event instanceof EffectTriggeredEvent) {
            applyEffect((EffectTriggeredEvent) event);

        } else if (event instanceof CardsPlayedEvent) {
            // Cards remain active; this event is informational.

        } else {
            throw new IllegalArgumentException(
                    "Unsupported replay event: "
                            + event.getType());
        }
    }

    private MutablePlayer player(String playerId) {

        for (MutablePlayer player : players) {
            if (player.playerId.equals(playerId)) {
                return player;
            }
        }

        throw new IllegalStateException(
                "Unknown player: " + playerId);
    }

    private void verifyInitialDraw(CardsDrawnEvent event) {

        for (DrawnCard drawn : event.getCards()) {

            MutablePlayer owner = player(drawn.getPlayerId());

            if (owner.activeCard == null
                    || !owner.activeCard.instanceId.equals(
                            drawn.getInstanceId())
                    || !owner.activeCard.cardId.equals(
                            drawn.getCardId())) {

                throw new IllegalStateException(
                        "Initial draw mismatch: "
                                + drawn.getInstanceId());
            }
        }
    }

    private void applyCardRoundAdvanced(
            CardRoundAdvancedEvent event) {

        MutablePlayer owner = player(event.getPlayerId());

        if (owner.activeCard == null
                || !owner.activeCard.instanceId.equals(
                        event.getInstanceId())) {

            throw new IllegalStateException(
                    "Round counter card mismatch");
        }

        if (event.getRoundsInPlay() != owner.activeCard.roundsInPlay + 1) {

            throw new IllegalStateException(
                    "Invalid roundsInPlay transition");
        }

        owner.activeCard.roundsInPlay = event.getRoundsInPlay();
    }

    private void applyCombat(CombatResolvedEvent event) {

        List<CombatCardResult> cards = event.getCards();

        if (cards.size() != 2) {
            throw new IllegalStateException(
                    "Combat must contain two cards");
        }

        for (int i = 0; i < 2; i++) {

            MutableCard active = players[i].activeCard;
            CombatCardResult result = cards.get(i);

            if (active == null
                    || !active.instanceId.equals(
                            result.getInstanceId())
                    || !active.cardId.equals(
                            result.getCardId())) {

                throw new IllegalStateException(
                        "Combat card mismatch");
            }

            // Verify defense before combat.
            requireValue(
                    "combatDefenseBefore",
                    active.currentDefense,
                    result.getDefenseBefore());

            // Verify protection before combat.
            if (result.getProtectionBefore() == null
                    || result.getProtectionAfter() == null) {

                throw new IllegalStateException(
                        "Missing combat protection data");
            }

            requireValue(
                    "combatProtectionBefore",
                    active.protection,
                    result.getProtectionBefore());

            // Apply the exact recorded combat result.
            active.currentDefense = result.getDefenseAfter();
            active.protection = result.getProtectionAfter();

            // Verify destruction consistency.
            if ((active.currentDefense <= 0) != result.isDestroyed()) {

                throw new IllegalStateException(
                        "Combat destruction mismatch");
            }
        }

        // PlayerDamage contains damage, not remaining health.
        players[0].health -= event.getPlayerDamage().getPlayerOne();

        players[1].health -= event.getPlayerDamage().getPlayerTwo();
    }

    private void applyDestruction(CardDestroyedEvent event) {

        MutablePlayer owner = player(event.getPlayerId());
        MutableCard active = owner.activeCard;

        if (active == null
                || !active.instanceId.equals(
                        event.getInstanceId())
                || !active.cardId.equals(event.getCardId())
                || active.currentDefense > 0) {

            throw new IllegalStateException(
                    "Invalid destroyed card: "
                            + event.getInstanceId());
        }

        for (MutableCard discarded : owner.discardPile) {
            if (discarded.instanceId.equals(active.instanceId)) {
                throw new IllegalStateException(
                        "Card already discarded");
            }
        }

        owner.discardPile.add(active);
        owner.lastDestroyedSchool = active.school;
    }

    private void applyReplacement(CardReplacedEvent event) {

        MutablePlayer owner = player(event.getPlayerId());

        if (owner.activeCard == null
                || !owner.activeCard.instanceId.equals(
                        event.getDestroyedInstanceId())
                || owner.activeCard.currentDefense > 0) {

            throw new IllegalStateException(
                    "Invalid card replacement");
        }

        owner.activeCard = null;

        if (event.getNewInstanceId() == null) {

            if (!owner.deck.isEmpty()) {
                throw new IllegalStateException(
                        "Missing replacement despite nonempty deck");
            }

            return;
        }

        if (owner.deck.isEmpty()) {
            throw new IllegalStateException(
                    "Replacement deck is empty");
        }

        MutableCard next = owner.deck.remove(0);

        if (!next.instanceId.equals(event.getNewInstanceId())
                || !next.cardId.equals(event.getNewCardId())) {

            throw new IllegalStateException(
                    "Replacement card mismatch");
        }

        owner.activeCard = next;
    }

    private void verifyRoundEnd(RoundEndedEvent event) {

        if (players[0].health != event.getPlayersLife().getPlayerOne()
                || players[1].health != event.getPlayersLife().getPlayerTwo()) {

            throw new IllegalStateException(
                    "Round end health mismatch");
        }
    }

    private static class MutablePlayer {

        private final String playerId;
        private int health;
        private MutableCard activeCard;
        private final List<MutableCard> deck = new ArrayList<>();
        private final List<MutableCard> discardPile = new ArrayList<>();
        private String lastDestroyedSchool;

        private MutablePlayer(ReplayPlayerState state) {

            playerId = state.getPlayerId();
            health = state.getHealth();

            if (state.getActiveCard() != null) {
                activeCard = new MutableCard(
                        state.getActiveCard());
            }

            for (ReplayCardState card : state.getDeck()) {
                deck.add(new MutableCard(card));
            }

            for (ReplayCardState card : state.getDiscardPile()) {
                discardPile.add(new MutableCard(card));
            }

            lastDestroyedSchool = state.getLastDestroyedSchool();
        }

        private ReplayPlayerState snapshot() {

            List<ReplayCardState> deckStates = new ArrayList<>();

            for (MutableCard card : deck) {
                deckStates.add(card.snapshot());
            }

            List<ReplayCardState> discardStates = new ArrayList<>();

            for (MutableCard card : discardPile) {
                discardStates.add(card.snapshot());
            }

            return new ReplayPlayerState(
                    playerId,
                    health,
                    activeCard == null
                            ? null
                            : activeCard.snapshot(),
                    deckStates,
                    discardStates,
                    lastDestroyedSchool);
        }
    }

    private static class MutableCard {

        private final String instanceId;
        private final String cardId;
        private final int attack;
        private final int defense;
        private final int cost;
        private final String echo;
        private final String school;
        private final String effect;

        private int currentDefense;
        private int maxDefense;
        private int protection;
        private int attackBonus;
        private int temporaryAttackBonus;
        private int roundsInPlay;
        private int healingDone;
        private String analysisTargetId;

        private MutableCard(ReplayCardState state) {

            instanceId = state.getInstanceId();
            cardId = state.getCardId();
            attack = state.getAttack();
            defense = state.getDefense();
            cost = state.getCost();
            echo = state.getEcho();
            school = state.getSchool();
            effect = state.getEffect();

            currentDefense = state.getCurrentDefense();
            maxDefense = state.getMaxDefense();
            protection = state.getProtection();
            attackBonus = state.getAttackBonus();
            temporaryAttackBonus = state.getTemporaryAttackBonus();
            roundsInPlay = state.getRoundsInPlay();
            healingDone = state.getHealingDone();
            analysisTargetId = state.getAnalysisTargetId();
        }

        private ReplayCardState snapshot() {

            return new ReplayCardState(
                    instanceId,
                    cardId,
                    attack,
                    defense,
                    cost,
                    echo,
                    school,
                    effect,
                    currentDefense,
                    maxDefense,
                    protection,
                    attackBonus,
                    temporaryAttackBonus,
                    roundsInPlay,
                    healingDone,
                    analysisTargetId);
        }
    }
}
