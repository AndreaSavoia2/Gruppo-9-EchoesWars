
package com.echoeswars.game;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.effects.EffectResolver;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import com.echoeswars.replay.ReplayRound;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RoundManagerTest {

    private RoundManager createRoundManager() {

        return new RoundManager(
                new CombatResolver(),
                new EffectResolver());
    }

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
            Card... cards) {

        ArrayList<Card> deck = new ArrayList<>();

        for (Card card : cards) {
            deck.add(card);
        }

        Player player = new Player(playerId, deck);

        // Initial draw happens before the first round.
        player.drawCard();

        return player;
    }

    @Test
    void shouldResolveCombatDuringRound() {

        Card first = createCard(
                "first",
                3,
                8,
                CardEffect.NONE);

        Card second = createCard(
                "second",
                2,
                8,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        RoundManager manager = createRoundManager();

        ReplayRound result = manager.playRound(
                1,
                player1,
                player2);

        assertNotNull(result);

        assertEquals(6, first.getCurrentDefense());
        assertEquals(5, second.getCurrentDefense());

        assertEquals(10, player1.getHealth());
        assertEquals(10, player2.getHealth());
    }

    @Test
    void shouldRegisterDestroyedCardAndDrawReplacement() {

        Card first = createCard(
                "first",
                1,
                2,
                CardEffect.NONE);

        Card replacement = createCard(
                "replacement",
                1,
                8,
                CardEffect.NONE);

        Card opponent = createCard(
                "opponent",
                3,
                8,
                CardEffect.NONE);

        Player player1 = createPlayer(
                "p1",
                first,
                replacement);

        Player player2 = createPlayer(
                "p2",
                opponent);

        RoundManager manager = createRoundManager();

        manager.playRound(1, player1, player2);

        assertEquals(1, player1.getDiscardPileSize());
        assertSame(first, player1.getDiscardPile().get(0));

        assertEquals(
                CardSchool.GUERRIERI,
                player1.getLastDestroyedSchool());

        assertSame(replacement, player1.getActiveCard());
    }

    @Test
    void shouldResolveSabotageOnEntry() {

        Card sabotage = createCard(
                "sabotage",
                1,
                8,
                CardEffect.SABOTAGGIO);

        Card opponent = createCard(
                "opponent",
                1,
                8,
                CardEffect.NONE);

        Card deckCard = createCard(
                "deck-card",
                1,
                8,
                CardEffect.NONE);

        Player player1 = createPlayer("p1", sabotage);
        Player player2 = createPlayer(
                "p2",
                opponent,
                deckCard);

        RoundManager manager = createRoundManager();

        manager.playRound(1, player1, player2);

        assertEquals(1, player2.getDiscardPileSize());
        assertSame(deckCard, player2.getDiscardPile().get(0));

        // Sabotage does not count as battlefield destruction.
        assertNull(player2.getLastDestroyedSchool());
    }

    @Test
    void shouldRejectInvalidRoundNumber() {

        Player player1 = createPlayer(
                "p1",
                createCard("first", 1, 8, CardEffect.NONE));

        Player player2 = createPlayer(
                "p2",
                createCard("second", 1, 8, CardEffect.NONE));

        RoundManager manager = createRoundManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.playRound(0, player1, player2));
    }
}
