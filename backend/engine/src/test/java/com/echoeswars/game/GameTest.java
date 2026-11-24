
package com.echoeswars.game;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Card createCard(
            String instanceId,
            String cardId,
            int attack,
            int defense,
            CardSchool school) {

        return new Card(
                instanceId,
                cardId,
                attack,
                defense,
                1,
                Echo.DOMINIO,
                school,
                CardEffect.NONE
        );
    }

    private Player createPlayer(
            String playerId,
            Card... cards) {

        ArrayList<Card> deck = new ArrayList<>();

        for (Card card : cards) {
            deck.add(card);
        }

        Player player = new Player(playerId, deck);

        // Game expects the initial card to be drawn already.
        player.drawCard();

        return player;
    }

    @Test
    void destroyedCardShouldBeRegisteredBeforeReplacement() {

        Card firstCard = createCard(
                "p1-c1", "C001", 6, 2,
                CardSchool.GUERRIERI
        );

        Card replacement = createCard(
                "p1-c2", "C002", 1, 8,
                CardSchool.MAGHI
        );

        Card opponentCard = createCard(
                "p2-c1", "C003", 3, 8,
                CardSchool.FURFANTI
        );

        Player player1 = createPlayer(
                "player-1", firstCard, replacement
        );

        Player player2 = createPlayer(
                "player-2", opponentCard
        );

        Game game = new Game(
                "test-match",
                "1.3",
                player1,
                player2
        );

        game.play();

        // The original card was destroyed and discarded.
        assertEquals(1, player1.getDiscardPileSize());
        assertSame(firstCard, player1.getDiscardPile().get(0));

        // The last destroyed school was updated.
        assertEquals(
                CardSchool.GUERRIERI,
                player1.getLastDestroyedSchool()
        );

        // The replacement must be a different instance.
        assertNotSame(
                firstCard,
                player1.getActiveCard()
        );
    }

    @Test
    void simultaneousDestructionShouldDiscardBothCards() {

        Card firstCard = createCard(
                "p1-c1", "C001", 5, 2,
                CardSchool.GUERRIERI
        );

        Card secondCard = createCard(
                "p2-c1", "C002", 5, 2,
                CardSchool.MAGHI
        );

        Player player1 = createPlayer(
                "player-1", firstCard
        );

        Player player2 = createPlayer(
                "player-2", secondCard
        );

        Game game = new Game(
                "test-match",
                "1.3",
                player1,
                player2
        );

        game.play();

        assertEquals(1, player1.getDiscardPileSize());
        assertEquals(1, player2.getDiscardPileSize());

        assertSame(firstCard, player1.getDiscardPile().get(0));
        assertSame(secondCard, player2.getDiscardPile().get(0));

        assertEquals(
                CardSchool.GUERRIERI,
                player1.getLastDestroyedSchool()
        );

        assertEquals(
                CardSchool.MAGHI,
                player2.getLastDestroyedSchool()
        );

        assertEquals(MatchStatus.DRAW, game.getStatus());
    }

    @Test
    void survivingCardShouldNotBeDiscarded() {

        Card firstCard = createCard(
                "p1-c1", "C001", 10, 8,
                CardSchool.GUERRIERI
        );

        Card secondCard = createCard(
                "p2-c1", "C002", 1, 2,
                CardSchool.MAGHI
        );

        Player player1 = createPlayer(
                "player-1", firstCard
        );

        Player player2 = createPlayer(
                "player-2", secondCard
        );

        Game game = new Game(
                "test-match",
                "1.3",
                player1,
                player2
        );

        game.play();

        assertEquals(0, player1.getDiscardPileSize());
        assertEquals(1, player2.getDiscardPileSize());

        assertSame(firstCard, player1.getActiveCard());

        assertNull(player1.getLastDestroyedSchool());
    }
}
