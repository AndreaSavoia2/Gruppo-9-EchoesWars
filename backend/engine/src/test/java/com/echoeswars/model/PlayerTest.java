
package com.echoeswars.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Card createCard(
            String instanceId,
            CardSchool school) {

        return new Card(
                instanceId,
                "C001",
                5,
                4,
                3,
                Echo.DOMINIO,
                school,
                CardEffect.NONE
        );
    }

    @Test
    void shouldStartWithTenHealth() {
        Player player = new Player(
                "player-1",
                new ArrayList<>()
        );

        assertEquals(10, player.getHealth());
    }

    @Test
    void shouldDiscardTopCard() {
        ArrayList<Card> deck = new ArrayList<>();

        deck.add(createCard("card-1", CardSchool.GUERRIERI));
        deck.add(createCard("card-2", CardSchool.MAGHI));

        Player player = new Player("player-1", deck);

        Card discarded = player.discardTopCard();

        assertEquals("card-1", discarded.getInstanceId());
        assertEquals(1, player.getDiscardPileSize());
        assertEquals(1, player.getDeck().size());
        assertNull(player.getLastDestroyedSchool());
    }

    @Test
    void shouldNotLoseWhenDiscardingFromEmptyDeck() {
        Player player = new Player(
                "player-1",
                new ArrayList<>()
        );

        assertNull(player.discardTopCard());
        assertEquals(0, player.getDiscardPileSize());
        assertEquals(10, player.getHealth());
    }

    @Test
    void shouldTrackLastDestroyedSchool() {
        Player player = new Player(
                "player-1",
                new ArrayList<>()
        );

        Card destroyed = createCard(
                "card-1",
                CardSchool.MAGHI
        );

        player.registerDestroyedCard(destroyed);

        assertEquals(
                CardSchool.MAGHI,
                player.getLastDestroyedSchool()
        );

        assertEquals(1, player.getDiscardPileSize());
    }

    @Test
    void shouldNotHealAboveTwenty() {
        Player player = new Player(
                "player-1",
                new ArrayList<>()
        );

        int restored = player.heal(15);

        assertEquals(10, restored);
        assertEquals(20, player.getHealth());
    }
}
