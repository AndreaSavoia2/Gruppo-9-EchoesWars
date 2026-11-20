
package com.echoeswars.effects;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.game.RoundManager;
import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SacrificeEffectTest {

    private Card createCard(
            String id,
            int attack,
            int defense,
            CardEffect effect) {

        return new Card(
                id,
                id,
                attack,
                defense,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                effect);
    }

    private Player createPlayer(String id, Card card) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(card);

        Player player = new Player(id, deck);
        player.drawCard();

        return player;
    }

    @Test
    void shouldHealTwoHpWhenDestroyed() {

        Card sacrifice = createCard(
                "sacrifice", 1, 2, CardEffect.SACRIFICIO);

        Player owner = createPlayer("p1", sacrifice);
        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(4);

        sacrifice.takeDamage(2);

        new EffectResolver().resolveDestructionEffects(
                owner, opponent);

        assertEquals(8, owner.getHealth());
    }

    @Test
    void shouldNotHealWhenCardSurvives() {

        Card sacrifice = createCard(
                "sacrifice", 1, 5, CardEffect.SACRIFICIO);

        Player owner = createPlayer("p1", sacrifice);
        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(4);

        new EffectResolver().resolveDestructionEffects(
                owner, opponent);

        assertEquals(6, owner.getHealth());
    }

    @Test
    void shouldNotExceedMaximumHealth() {

        Card sacrifice = createCard(
                "sacrifice", 1, 2, CardEffect.SACRIFICIO);

        Player owner = createPlayer("p1", sacrifice);
        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.heal(9);

        sacrifice.takeDamage(2);

        new EffectResolver().resolveDestructionEffects(
                owner, opponent);

        assertEquals(20, owner.getHealth());
    }

    @Test
    void shouldNotReviveDefeatedPlayer() {

        Card sacrifice = createCard(
                "sacrifice", 1, 2, CardEffect.SACRIFICIO);

        Player owner = createPlayer("p1", sacrifice);
        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(10);
        sacrifice.takeDamage(2);

        new EffectResolver().resolveDestructionEffects(
                owner, opponent);

        assertEquals(0, owner.getHealth());
    }

    @Test
    void shouldHealBothPlayersAfterSimultaneousDestruction() {

        Card first = createCard(
                "first", 2, 2, CardEffect.SACRIFICIO);

        Card second = createCard(
                "second", 2, 2, CardEffect.SACRIFICIO);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        player1.takeDamage(3);
        player2.takeDamage(4);

        new CombatResolver().resolve(player1, player2);

        new EffectResolver().resolveDestructionEffects(
                player1, player2);

        assertTrue(first.isDestroyed());
        assertTrue(second.isDestroyed());

        assertEquals(9, player1.getHealth());
        assertEquals(8, player2.getHealth());
    }

    @Test
    void shouldWorkThroughRoundManager() {

        Card sacrifice = createCard(
                "sacrifice", 1, 2, CardEffect.SACRIFICIO);

        Card enemy = createCard(
                "enemy", 3, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", sacrifice);
        Player opponent = createPlayer("p2", enemy);

        owner.takeDamage(4);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertTrue(sacrifice.isDestroyed());
        // Initial HP: 6.
        // Enemy attack: 3 against 2 DEF = 1 overflow damage.
        // Sacrifice heals 2 HP.
        // Final HP: 6 - 1 + 2 = 7.
        assertEquals(7, owner.getHealth());
    }

    @Test
    void shouldNotTriggerWhenDiscardedFromDeck() {

        Card active = createCard(
                "active", 1, 10, CardEffect.NONE);

        Card sacrifice = createCard(
                "sacrifice", 1, 2, CardEffect.SACRIFICIO);

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(active);
        deck.add(sacrifice);

        Player owner = new Player("p1", deck);
        owner.drawCard();

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(4);
        owner.discardTopCard();

        new EffectResolver().resolveDestructionEffects(
                owner, opponent);

        assertEquals(6, owner.getHealth());
    }
}
