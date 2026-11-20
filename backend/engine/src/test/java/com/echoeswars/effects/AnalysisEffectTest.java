
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

class AnalysisEffectTest {

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

    private RoundManager createManager() {
        return new RoundManager(
                new CombatResolver(),
                new EffectResolver());
    }

    @Test
    void shouldGainProtectionAgainstHigherBaseAttack() {

        Card analysis = createCard(
                "analysis", 1, 10, CardEffect.ANALISI);

        Card enemy = createCard(
                "enemy", 2, 10, CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", analysis),
                createPlayer("p2", enemy));

        // Analysis absorbs 1 of the enemy's 2 attack damage.
        assertEquals(9, analysis.getCurrentDefense());
        assertEquals(0, analysis.getProtection());
    }

    @Test
    void shouldNotActivateAgainstEqualAttack() {

        Card analysis = createCard(
                "analysis", 2, 10, CardEffect.ANALISI);

        Card enemy = createCard(
                "enemy", 2, 10, CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", analysis),
                createPlayer("p2", enemy));

        assertEquals(8, analysis.getCurrentDefense());
        assertEquals(0, analysis.getProtection());
    }

    @Test
    void shouldNotActivateAgainstLowerAttack() {

        Card analysis = createCard(
                "analysis", 3, 10, CardEffect.ANALISI);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", analysis),
                createPlayer("p2", enemy));

        assertEquals(9, analysis.getCurrentDefense());
    }

    @Test
    void shouldNotRegenerateProtectionAgainstSameOpponent() {

        Card analysis = createCard(
                "analysis", 1, 20, CardEffect.ANALISI);

        Card enemy = createCard(
                "enemy", 2, 20, CardEffect.NONE);

        Player owner = createPlayer("p1", analysis);
        Player opponent = createPlayer("p2", enemy);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);
        assertEquals(19, analysis.getCurrentDefense());

        manager.playRound(2, owner, opponent);

        // The second attack deals full damage: no new protection.
        assertEquals(17, analysis.getCurrentDefense());
    }

    @Test
    void shouldActivateAgainstNewOpponent() {

        Card analysis = createCard(
                "analysis", 3, 20, CardEffect.ANALISI);

        Card firstEnemy = createCard(
                "firstEnemy", 4, 2, CardEffect.NONE);

        Card secondEnemy = createCard(
                "secondEnemy", 5, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", analysis);
        Player opponent = createPlayer(
                "p2", firstEnemy, secondEnemy);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);

        assertTrue(firstEnemy.isDestroyed());
        assertEquals(17, analysis.getCurrentDefense());

        manager.playRound(2, owner, opponent);

        // New opponent grants another 1 protection.
        // Attack 5 - protection 1 = 4 DEF damage.
        assertEquals(13, analysis.getCurrentDefense());
    }

    @Test
    void shouldComparePrintedAttackOnly() {

        Card analysis = createCard(
                "analysis", 2, 10, CardEffect.ANALISI);

        Card enemy = createCard(
                "enemy", 1, 10, CardEffect.NONE);

        // Effective attack is higher, but printed attack is not.
        enemy.addAttackBonus(3);

        createManager().playRound(
                1,
                createPlayer("p1", analysis),
                createPlayer("p2", enemy));

        assertEquals(6, analysis.getCurrentDefense());
        assertEquals(0, analysis.getProtection());
    }

    @Test
    void shouldRemoveRemainingProtectionWhenEnemyDies() {

        Card analysis = createCard(
                "analysis", 3, 10, CardEffect.ANALISI);

        Card enemy = createCard(
                "enemy", 4, 2, CardEffect.NONE);

        // Extra protection allows some protection to remain
        // after the enemy attack.
        analysis.addProtection(5);

        Player owner = createPlayer("p1", analysis);
        Player opponent = createPlayer("p2", enemy);

        createManager().playRound(1, owner, opponent);

        assertTrue(enemy.isDestroyed());
        assertEquals(0, analysis.getProtection());
    }
}
