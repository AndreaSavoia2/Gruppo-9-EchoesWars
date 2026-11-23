
package com.echoeswars.effects;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CurseEffectTest {

    private Card createCard(
            String instanceId,
            int defense,
            CardEffect effect) {

        return new Card(
                instanceId,
                instanceId,
                2,
                defense,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                effect);
    }

    private Player createPlayer(
            String playerId,
            Card activeCard) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(activeCard);

        Player player = new Player(playerId, deck);
        player.drawCard();

        return player;
    }

    @Test
    void shouldReduceExistingOpponentDefenseByTwo() {

        Card curse = createCard(
                "curse",
                6,
                CardEffect.MALEDIZIONE);

        Card opponentCard = createCard(
                "opponent",
                8,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", curse);
        Player player2 = createPlayer("p2", opponentCard);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(6, opponentCard.getCurrentDefense());

        // Printed and maximum defense remain unchanged.
        assertEquals(8, opponentCard.getDefense());
        assertEquals(8, opponentCard.getMaxDefense());
    }

    @Test
    void shouldNotReduceDefenseBelowOne() {

        Card curse = createCard(
                "curse",
                6,
                CardEffect.MALEDIZIONE);

        Card opponentCard = createCard(
                "opponent",
                2,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", curse);
        Player player2 = createPlayer("p2", opponentCard);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(1, opponentCard.getCurrentDefense());
        assertFalse(opponentCard.isDestroyed());
    }

    @Test
    void shouldNotAffectSimultaneouslyEnteringCard() {

        Card curse = createCard(
                "curse",
                6,
                CardEffect.MALEDIZIONE);

        Card opponentCard = createCard(
                "opponent",
                8,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", curse);
        Player player2 = createPlayer("p2", opponentCard);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                true);

        assertEquals(8, opponentCard.getCurrentDefense());
    }

    @Test
    void shouldNotActivateAgainWithoutNewEntry() {

        Card curse = createCard(
                "curse",
                6,
                CardEffect.MALEDIZIONE);

        Card opponentCard = createCard(
                "opponent",
                8,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", curse);
        Player player2 = createPlayer("p2", opponentCard);

        EffectResolver resolver = new EffectResolver();

        // Curse enters while the opponent card is already present.
        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(6, opponentCard.getCurrentDefense());

        // Neither card enters in the following round.
        resolver.resolveEntryEffects(
                player1,
                player2,
                false,
                false);

        assertEquals(6, opponentCard.getCurrentDefense());
    }

    @Test
    void shouldNotAffectDestroyedOpponentCard() {

        Card curse = createCard(
                "curse",
                6,
                CardEffect.MALEDIZIONE);

        Card opponentCard = createCard(
                "opponent",
                2,
                CardEffect.NONE);

        opponentCard.takeDamage(2);

        Player player1 = createPlayer("p1", curse);
        Player player2 = createPlayer("p2", opponentCard);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(0, opponentCard.getCurrentDefense());
    }

    @Test
    void shouldPreserveSabotageBehavior() {

        Card sabotage = createCard(
                "sabotage",
                6,
                CardEffect.SABOTAGGIO);

        Card opponentCard = createCard(
                "opponent",
                8,
                CardEffect.NONE);

        Card deckCard = createCard(
                "deck-card",
                4,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", sabotage);

        ArrayList<Card> opponentDeck = new ArrayList<>();
        opponentDeck.add(opponentCard);
        opponentDeck.add(deckCard);

        Player player2 = new Player("p2", opponentDeck);
        player2.drawCard();

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                true);

        assertEquals(1, player2.getDiscardPileSize());
        assertSame(deckCard, player2.getDiscardPile().get(0));
    }
}
