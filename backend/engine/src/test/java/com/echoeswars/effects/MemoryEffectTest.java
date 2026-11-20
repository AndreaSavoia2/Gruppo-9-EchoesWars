
package com.echoeswars.effects;

import com.echoeswars.combat.CombatResolver;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MemoryEffectTest {

    private Card createCard(
            String instanceId,
            int attack,
            int defense,
            CardEffect effect) {

        return new Card(
                instanceId,
                instanceId,
                attack,
                defense,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                effect);
    }

    private Player createPlayer(
            String playerId,
            Card activeCard,
            int discardCount) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(activeCard);

        for (int i = 0; i < discardCount; i++) {

            deck.add(createCard(
                    playerId + "-discard-" + i,
                    1,
                    5,
                    CardEffect.NONE));
        }

        Player player = new Player(playerId, deck);

        // Draw the active card first.
        player.drawCard();

        // Populate the discard pile through the public API.
        for (int i = 0; i < discardCount; i++) {
            player.discardTopCard();
        }

        return player;
    }

    private int resolveMemoryBonus(int discardCount) {

        Card memory = createCard(
                "memory",
                2,
                20,
                CardEffect.MEMORIA);

        Card opponentCard = createCard(
                "opponent",
                2,
                20,
                CardEffect.NONE);

        Player player1 = createPlayer(
                "p1",
                memory,
                discardCount);

        Player player2 = createPlayer(
                "p2",
                opponentCard,
                0);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        return memory.getAttackBonus();
    }

    @Test
    void shouldGiveNoBonusWithZeroDiscards() {

        assertEquals(0, resolveMemoryBonus(0));
    }

    @Test
    void shouldGiveNoBonusWithFourDiscards() {

        assertEquals(0, resolveMemoryBonus(4));
    }

    @Test
    void shouldGiveOneBonusWithFiveDiscards() {

        assertEquals(1, resolveMemoryBonus(5));
    }

    @Test
    void shouldGiveTwoBonusWithTenDiscards() {

        assertEquals(2, resolveMemoryBonus(10));
    }

    @Test
    void shouldGiveThreeBonusWithFifteenDiscards() {

        assertEquals(3, resolveMemoryBonus(15));
    }

    @Test
    void shouldCapBonusAtThree() {

        assertEquals(3, resolveMemoryBonus(20));
    }

    @Test
    void shouldNotRecalculateBonusOnLaterRounds() {

        Card memory = createCard(
                "memory",
                2,
                20,
                CardEffect.MEMORIA);

        Card opponentCard = createCard(
                "opponent",
                2,
                20,
                CardEffect.NONE);

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(memory);

        for (int i = 0; i < 10; i++) {
            deck.add(createCard(
                    "discard-" + i,
                    1,
                    5,
                    CardEffect.NONE));
        }

        Player player1 = new Player("p1", deck);
        player1.drawCard();

        // Five cards are discarded before Memory enters.
        for (int i = 0; i < 5; i++) {
            player1.discardTopCard();
        }

        Player player2 = createPlayer(
                "p2",
                opponentCard,
                0);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(1, memory.getAttackBonus());

        // Discard five additional cards after entry.
        for (int i = 0; i < 5; i++) {
            player1.discardTopCard();
        }

        assertEquals(10, player1.getDiscardPileSize());

        // No new entry: the original bonus must remain unchanged.
        resolver.resolveEntryEffects(
                player1,
                player2,
                false,
                false);

        assertEquals(1, memory.getAttackBonus());
    }

    @Test
    void shouldUseDiscardSnapshotBeforeSabotage() {

        Card sabotage = createCard(
                "sabotage",
                1,
                20,
                CardEffect.SABOTAGGIO);

        Card memory = createCard(
                "memory",
                2,
                20,
                CardEffect.MEMORIA);

        // Player 1 enters with Sabotage.
        Player player1 = createPlayer("p1", sabotage, 0);

        // Player 2 starts with four discards.
        Player player2 = createPlayer("p2", memory, 4);

        // Add one more card to player 2's deck.
        // The deck returned by getDeck() is a copy, so build
        // a dedicated player instead for this scenario.
        ArrayList<Card> deck = new ArrayList<>();

        Card secondMemory = createCard(
                "memory-2",
                2,
                20,
                CardEffect.MEMORIA);

        deck.add(secondMemory);

        for (int i = 0; i < 4; i++) {
            deck.add(createCard(
                    "discard-" + i,
                    1,
                    5,
                    CardEffect.NONE));
        }

        Card sabotageTarget = createCard(
                "sabotage-target",
                1,
                5,
                CardEffect.NONE);

        deck.add(sabotageTarget);

        Player memoryPlayer = new Player("p2", deck);
        memoryPlayer.drawCard();

        for (int i = 0; i < 4; i++) {
            memoryPlayer.discardTopCard();
        }

        assertEquals(4, memoryPlayer.getDiscardPileSize());

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                memoryPlayer,
                true,
                true);

        // Sabotage increases the discard pile to five.
        assertEquals(5, memoryPlayer.getDiscardPileSize());

        // Memory must use the snapshot of four.
        assertEquals(0, secondMemory.getAttackBonus());
    }

    @Test
    void shouldIncludeMemoryBonusInCombat() {

        Card memory = createCard(
                "memory",
                2,
                20,
                CardEffect.MEMORIA);

        Card opponentCard = createCard(
                "opponent",
                2,
                20,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", memory, 10);
        Player player2 = createPlayer("p2", opponentCard, 0);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(2, memory.getAttackBonus());
        assertEquals(4, memory.getBaseEffectiveAttack());

        // Both cards have the same Echo, so no Echo bonus applies.
        new CombatResolver().resolve(player1, player2);

        assertEquals(16, opponentCard.getCurrentDefense());
        assertEquals(18, memory.getCurrentDefense());
    }
}
