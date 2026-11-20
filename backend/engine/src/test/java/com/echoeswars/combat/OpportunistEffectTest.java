
package com.echoeswars.combat;

import com.echoeswars.effects.EffectResolver;
import com.echoeswars.game.RoundManager;
import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OpportunistEffectTest {

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

    private RoundManager createManager() {
        return new RoundManager(
                new CombatResolver(),
                new EffectResolver());
    }

    @Test
    void shouldNotGainBonusAgainstFullDefense() {

        Card opportunist = createCard(
                "opportunist", 2, 10, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", opportunist),
                createPlayer("p2", enemy));

        // The enemy starts at full DEF.
        assertEquals(8, enemy.getCurrentDefense());
    }

    @Test
    void shouldGainBonusAgainstDamagedEnemy() {

        Card opportunist = createCard(
                "opportunist", 2, 10, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        enemy.takeDamage(2);

        createManager().playRound(
                1,
                createPlayer("p1", opportunist),
                createPlayer("p2", enemy));

        // Enemy starts at 8 DEF.
        // Opportunist deals 2 + 1 = 3 damage.
        assertEquals(5, enemy.getCurrentDefense());
    }

    @Test
    void shouldActivateFromSecondRoundAfterDealingDamage() {

        Card opportunist = createCard(
                "opportunist", 2, 20, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 20, CardEffect.NONE);

        Player owner = createPlayer("p1", opportunist);
        Player opponent = createPlayer("p2", enemy);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);

        // No bonus during the first round.
        assertEquals(18, enemy.getCurrentDefense());

        manager.playRound(2, owner, opponent);

        // The enemy was already wounded at round start.
        assertEquals(15, enemy.getCurrentDefense());
    }

    @Test
    void shouldNotAccumulateBonus() {

        Card opportunist = createCard(
                "opportunist", 1, 20, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 20, CardEffect.NONE);

        enemy.takeDamage(1);

        Player owner = createPlayer("p1", opportunist);
        Player opponent = createPlayer("p2", enemy);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);
        assertEquals(17, enemy.getCurrentDefense());

        manager.playRound(2, owner, opponent);
        assertEquals(15, enemy.getCurrentDefense());

        manager.playRound(3, owner, opponent);
        assertEquals(13, enemy.getCurrentDefense());

        assertEquals(1, opportunist.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotActivateFromProtectionLossAlone() {

        Card opportunist = createCard(
                "opportunist", 2, 10, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        enemy.addProtection(2);

        // Protection is lost, but DEF remains full.
        enemy.takeDamage(1);

        assertEquals(10, enemy.getCurrentDefense());
        assertEquals(1, enemy.getProtection());

        createManager().playRound(
                1,
                createPlayer("p1", opportunist),
                createPlayer("p2", enemy));

        // The attack deals 2, not 3.
        // Remaining Protection absorbs 1 damage.
        assertEquals(9, enemy.getCurrentDefense());
    }

    @Test
    void shouldUseEffectiveMaximumDefense() {

        Card opportunist = createCard(
                "opportunist", 2, 10, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        // Simulate a permanent DEF increase.
        enemy.increaseMaxDefense(2);

        // Effective maximum DEF is now 12.
        enemy.takeDamage(1);

        createManager().playRound(
                1,
                createPlayer("p1", opportunist),
                createPlayer("p2", enemy));

        // Enemy starts at 11 DEF and receives 3 damage.
        assertEquals(8, enemy.getCurrentDefense());
    }

    @Test
    void shouldNotModifyPermanentAttack() {

        Card opportunist = createCard(
                "opportunist", 2, 10, CardEffect.OPPORTUNISTA);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        enemy.takeDamage(1);

        createManager().playRound(
                1,
                createPlayer("p1", opportunist),
                createPlayer("p2", enemy));

        assertEquals(0, opportunist.getAttackBonus());
        assertEquals(0, opportunist.getTemporaryAttackBonus());
        assertEquals(2, opportunist.getBaseEffectiveAttack());
    }

    @Test
    void shouldRecalculateWhenOpponentChanges() {

        Card opportunist = createCard(
                "opportunist", 2, 20, CardEffect.OPPORTUNISTA);

        Card firstEnemy = createCard(
                "firstEnemy", 1, 2, CardEffect.NONE);

        Card secondEnemy = createCard(
                "secondEnemy", 1, 10, CardEffect.NONE);

        ArrayList<Card> opponentDeck = new ArrayList<>();
        opponentDeck.add(firstEnemy);
        opponentDeck.add(secondEnemy);

        Player owner = createPlayer("p1", opportunist);
        Player opponent = new Player("p2", opponentDeck);
        opponent.drawCard();

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);

        assertTrue(firstEnemy.isDestroyed());
        assertSame(secondEnemy, opponent.getActiveCard());

        manager.playRound(2, owner, opponent);

        // The new enemy starts at full DEF.
        // Opportunist does not receive its bonus.
        assertEquals(8, secondEnemy.getCurrentDefense());
    }
}
