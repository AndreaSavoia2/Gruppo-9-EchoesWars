
package com.echoeswars.effects;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EffectResolverTest {

    private final EffectResolver resolver = new EffectResolver();

    private Card createCard(
            String instanceId,
            CardEffect effect) {

        return new Card(
                instanceId,
                instanceId,
                1,
                8,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                effect
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
        player.drawCard();

        return player;
    }

    @Test
    void sabotageShouldDiscardOneOpponentCard() {

        Card sabotage = createCard(
                "sabotage",
                CardEffect.SABOTAGGIO
        );

        Card opponent = createCard(
                "opponent",
                CardEffect.NONE
        );

        Card discarded = createCard(
                "discarded",
                CardEffect.NONE
        );

        Player player1 = createPlayer("p1", sabotage);
        Player player2 = createPlayer("p2", opponent, discarded);

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                true
        );

        assertEquals(1, player2.getDiscardPileSize());
        assertSame(discarded, player2.getDiscardPile().get(0));

        // Deck discards are not battlefield destructions.
        assertNull(player2.getLastDestroyedSchool());
    }

    @Test
    void bothSabotageCardsShouldActivate() {

        Card firstSabotage = createCard(
                "sabotage-1",
                CardEffect.SABOTAGGIO
        );

        Card secondSabotage = createCard(
                "sabotage-2",
                CardEffect.SABOTAGGIO
        );

        Card firstDiscard = createCard(
                "discard-1",
                CardEffect.NONE
        );

        Card secondDiscard = createCard(
                "discard-2",
                CardEffect.NONE
        );

        Player player1 = createPlayer(
                "p1",
                firstSabotage,
                firstDiscard
        );

        Player player2 = createPlayer(
                "p2",
                secondSabotage,
                secondDiscard
        );

        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                true
        );

        assertEquals(1, player1.getDiscardPileSize());
        assertEquals(1, player2.getDiscardPileSize());

        assertSame(firstDiscard, player1.getDiscardPile().get(0));
        assertSame(secondDiscard, player2.getDiscardPile().get(0));
    }

    @Test
    void survivingCardShouldNotTriggerEntryEffectAgain() {

        Card sabotage = createCard(
                "sabotage",
                CardEffect.SABOTAGGIO
        );

        Card opponent = createCard(
                "opponent",
                CardEffect.NONE
        );

        Card firstDiscard = createCard(
                "discard-1",
                CardEffect.NONE
        );

        Card secondDiscard = createCard(
                "discard-2",
                CardEffect.NONE
        );

        Player player1 = createPlayer("p1", sabotage);
        Player player2 = createPlayer(
                "p2",
                opponent,
                firstDiscard,
                secondDiscard
        );

        // First round: Sabotage enters.
        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                true
        );

        // Next round: neither card enters.
        resolver.resolveEntryEffects(
                player1,
                player2,
                false,
                false
        );

        assertEquals(1, player2.getDiscardPileSize());
        assertSame(firstDiscard, player2.getDiscardPile().get(0));
        assertEquals(1, player2.getDeck().size());
    }

    @Test
    void sabotageShouldIgnoreEmptyOpponentDeck() {

        Card sabotage = createCard(
                "sabotage",
                CardEffect.SABOTAGGIO
        );

        Card opponent = createCard(
                "opponent",
                CardEffect.NONE
        );

        Player player1 = createPlayer("p1", sabotage);
        Player player2 = createPlayer("p2", opponent);

        assertDoesNotThrow(() ->
                resolver.resolveEntryEffects(
                        player1,
                        player2,
                        true,
                        true
                )
        );

        assertEquals(0, player2.getDiscardPileSize());
        assertEquals(10, player2.getHealth());
    }

    @Test
    void unimplementedEffectsShouldBeIgnored() {

        Card fury = createCard(
                "fury",
                CardEffect.FURIA
        );

        Card opponent = createCard(
                "opponent",
                CardEffect.NONE
        );

        Card deckCard = createCard(
                "deck-card",
                CardEffect.NONE
        );

        Player player1 = createPlayer("p1", fury);
        Player player2 = createPlayer("p2", opponent, deckCard);

        assertDoesNotThrow(() ->
                resolver.resolveEntryEffects(
                        player1,
                        player2,
                        true,
                        true
                )
        );

        assertEquals(0, player2.getDiscardPileSize());
        assertEquals(1, player2.getDeck().size());
    }
}
