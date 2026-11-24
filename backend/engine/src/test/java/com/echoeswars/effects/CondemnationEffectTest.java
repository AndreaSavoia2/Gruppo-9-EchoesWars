
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

class CondemnationEffectTest {

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

    private Player createPlayer(
            String id,
            Card active,
            Card... remainingCards) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(active);

        for (Card card : remainingCards) {
            deck.add(card);
        }

        Player player = new Player(id, deck);
        player.drawCard();

        return player;
    }

    @Test
    void shouldDiscardOpponentTopCardWhenDestroyed() {

        Card condemned = createCard(
                "condemned", 1, 2, CardEffect.CONDANNA);

        Card enemy = createCard(
                "enemy", 3, 10, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", condemned);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertTrue(condemned.isDestroyed());
        assertTrue(opponent.getDiscardPile().contains(deckCard));
        assertEquals(1, opponent.getDiscardPileSize());
        assertEquals(0, opponent.getDeck().size());
    }

    @Test
    void shouldNotTriggerWhenCardSurvives() {

        Card condemned = createCard(
                "condemned", 1, 10, CardEffect.CONDANNA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", condemned);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertFalse(condemned.isDestroyed());
        assertFalse(opponent.getDiscardPile().contains(deckCard));
        assertEquals(1, opponent.getDeck().size());
    }

    @Test
    void shouldTriggerAfterSimultaneousDestruction() {

        Card first = createCard(
                "first", 2, 2, CardEffect.CONDANNA);

        Card second = createCard(
                "second", 2, 2, CardEffect.CONDANNA);

        Card firstDeckCard = createCard(
                "firstDeckCard", 1, 5, CardEffect.NONE);

        Card secondDeckCard = createCard(
                "secondDeckCard", 1, 5, CardEffect.NONE);

        Player player1 = createPlayer(
                "p1", first, firstDeckCard);

        Player player2 = createPlayer(
                "p2", second, secondDeckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, player1, player2);

        assertTrue(first.isDestroyed());
        assertTrue(second.isDestroyed());

        assertTrue(
                player1.getDiscardPile().contains(firstDeckCard));

        assertTrue(
                player2.getDiscardPile().contains(secondDeckCard));
    }

    @Test
    void shouldHandleEmptyOpponentDeck() {

        Card condemned = createCard(
                "condemned", 1, 2, CardEffect.CONDANNA);

        Card enemy = createCard(
                "enemy", 3, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", condemned);
        Player opponent = createPlayer("p2", enemy);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        assertDoesNotThrow(
                () -> manager.playRound(1, owner, opponent));

        assertTrue(condemned.isDestroyed());
        assertEquals(0, opponent.getDiscardPileSize());
    }

    @Test
    void shouldNotTriggerWhenDiscardedFromDeck() {

        Card active = createCard(
                "active", 1, 10, CardEffect.NONE);

        Card condemned = createCard(
                "condemned", 1, 2, CardEffect.CONDANNA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        Card enemyDeckCard = createCard(
                "enemyDeckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer(
                "p1", active, condemned);

        Player opponent = createPlayer(
                "p2", enemy, enemyDeckCard);

        owner.discardTopCard();

        EffectResolver resolver = new EffectResolver();

        resolver.resolveDestructionEffects(owner, opponent);

        assertFalse(
                opponent.getDiscardPile().contains(enemyDeckCard));

        assertEquals(1, opponent.getDeck().size());
    }

    @Test
    void shouldTriggerWhenDestroyedByReflection() {

        Card condemned = createCard(
                "condemned", 1, 2, CardEffect.CONDANNA);

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", condemned);
        Player opponent = createPlayer(
                "p2", reflector, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        // Ordinary attack leaves Condemnation at 1 DEF.
        // Reflection destroys it.
        assertTrue(condemned.isDestroyed());

        assertTrue(
                opponent.getDiscardPile().contains(deckCard));
    }

    @Test
    void shouldNotTriggerAfterHpDefeat() {

        Card condemned = createCard(
                "condemned", 1, 1, CardEffect.CONDANNA);

        Card enemy = createCard(
                "enemy", 5, 10, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", condemned);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        // Owner will lose the match from attack overflow.
        owner.takeDamage(8);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertTrue(condemned.isDestroyed());
        assertTrue(owner.getHealth() <= 0);

        // The HP defeat prevents destruction effects.
        assertFalse(
                opponent.getDiscardPile().contains(deckCard));
    }
}
