
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

class DrainEffectTest {

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
    void shouldDiscardWhenAttackDestroysEnemyAndDrainSurvives() {

        Card drain = createCard(
                "drain", 3, 10, CardEffect.DRENAGGIO);

        Card enemy = createCard(
                "enemy", 1, 2, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", drain);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        // The enemy is destroyed and registered as a discard.
        // Drain also discards the next card from the deck.
        assertEquals(2, opponent.getDiscardPileSize());
        assertTrue(opponent.getDiscardPile().contains(deckCard));
    }

    @Test
    void shouldNotDiscardWhenEnemySurvives() {

        Card drain = createCard(
                "drain", 1, 10, CardEffect.DRENAGGIO);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", drain);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertEquals(0, opponent.getDiscardPileSize());
        assertEquals(1, opponent.getDeck().size());
    }

    @Test
    void shouldNotDiscardWhenDrainIsDestroyed() {

        Card drain = createCard(
                "drain", 3, 2, CardEffect.DRENAGGIO);

        Card enemy = createCard(
                "enemy", 3, 2, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", drain);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertTrue(drain.isDestroyed());
        assertFalse(opponent.getDiscardPile().contains(deckCard));
    }

    @Test
    void shouldNotDiscardWhenDrainDiesFromReflection() {

        Card drain = createCard(
                "drain", 2, 2, CardEffect.DRENAGGIO);

        Card reflector = createCard(
                "reflector", 1, 2, CardEffect.RIFLESSO);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", drain);
        Player opponent = createPlayer(
                "p2", reflector, deckCard);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        // Ordinary attack leaves Drain at 1 DEF.
        // Reflection then destroys Drain.
        assertTrue(drain.isDestroyed());
        assertFalse(opponent.getDiscardPile().contains(deckCard));
    }

    @Test
    void shouldHandleEmptyOpponentDeck() {

        Card drain = createCard(
                "drain", 3, 10, CardEffect.DRENAGGIO);

        Card enemy = createCard(
                "enemy", 1, 2, CardEffect.NONE);

        Player owner = createPlayer("p1", drain);
        Player opponent = createPlayer("p2", enemy);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        assertDoesNotThrow(
                () -> manager.playRound(1, owner, opponent));

        assertTrue(enemy.isDestroyed());
        assertEquals(1, opponent.getDiscardPileSize());
    }

    @Test
    void shouldNotTriggerWhenAttackDidNotCauseKill() {

        Card drain = createCard(
                "drain", 1, 10, CardEffect.DRENAGGIO);

        Card enemy = createCard(
                "enemy", 1, 5, CardEffect.NONE);

        Card deckCard = createCard(
                "deckCard", 1, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", drain);
        Player opponent = createPlayer(
                "p2", enemy, deckCard);

        // Simulate a destroyed enemy without an attack kill.
        enemy.takeDamage(5);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveAfterCombatEffects(
                owner,
                opponent,
                0,
                0,
                false,
                false);

        assertFalse(opponent.getDiscardPile().contains(deckCard));
    }
}
