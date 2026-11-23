
package com.echoeswars.effects;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.effects.handlers.BerserkerEffect;
import com.echoeswars.game.RoundManager;
import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BerserkerEffectTest {
    private final BerserkerEffect berserkerEffect = new BerserkerEffect();

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

    private void startRound(
            Card card,
            Player owner,
            Player opponent) {

        card.incrementRoundsInPlay();

        berserkerEffect.onRoundStart(
                new EffectContext(card, owner, opponent));
    }

    @Test
    void shouldNotActivateDuringFirstRound() {

        Card berserker = createCard(
                "berserker", 2, 8, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        startRound(berserker, owner, opponent);

        assertEquals(8, berserker.getCurrentDefense());
        assertEquals(0, berserker.getAttackBonus());
    }

    @Test
    void shouldLoseOneDefenseAndGainTwoAttackInSecondRound() {

        Card berserker = createCard(
                "berserker", 2, 8, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        startRound(berserker, owner, opponent);
        startRound(berserker, owner, opponent);

        assertEquals(7, berserker.getCurrentDefense());
        assertEquals(2, berserker.getAttackBonus());
        assertEquals(4, berserker.getBaseEffectiveAttack());
    }

    @Test
    void shouldReachMaximumBonusInThirdRound() {

        Card berserker = createCard(
                "berserker", 2, 8, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        for (int i = 0; i < 3; i++) {
            startRound(berserker, owner, opponent);
        }

        assertEquals(6, berserker.getCurrentDefense());
        assertEquals(4, berserker.getAttackBonus());
        assertEquals(6, berserker.getBaseEffectiveAttack());
    }

    @Test
    void shouldNeverReduceDefenseBelowOne() {

        Card berserker = createCard(
                "berserker", 2, 2, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        for (int i = 0; i < 3; i++) {
            startRound(berserker, owner, opponent);
        }

        assertEquals(1, berserker.getCurrentDefense());
        assertEquals(2, berserker.getAttackBonus());
    }

    @Test
    void shouldNotActivateWhenDefenseIsOne() {

        Card berserker = createCard(
                "berserker", 2, 1, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        startRound(berserker, owner, opponent);
        startRound(berserker, owner, opponent);

        assertEquals(1, berserker.getCurrentDefense());
        assertEquals(0, berserker.getAttackBonus());
    }

    @Test
    void shouldNotExceedFourAttackBonus() {

        Card berserker = createCard(
                "berserker", 2, 8, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        for (int i = 0; i < 10; i++) {
            startRound(berserker, owner, opponent);
        }

        assertEquals(6, berserker.getCurrentDefense());
        assertEquals(4, berserker.getAttackBonus());
    }

    @Test
    void shouldPreservePrintedAttackAndMaxDefense() {

        Card berserker = createCard(
                "berserker", 2, 8, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer(
                "p2", createCard("enemy", 1, 8, CardEffect.NONE));

        startRound(berserker, owner, opponent);
        startRound(berserker, owner, opponent);

        assertEquals(2, berserker.getAttack());
        assertEquals(8, berserker.getMaxDefense());
        assertEquals(7, berserker.getCurrentDefense());
    }

    @Test
    void shouldWorkThroughRoundManager() {

        Card berserker = createCard(
                "berserker", 1, 8, CardEffect.BERSERKER);

        Card enemy = createCard(
                "enemy", 1, 8, CardEffect.NONE);

        Player owner = createPlayer("p1", berserker);
        Player opponent = createPlayer("p2", enemy);

        RoundManager roundManager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        roundManager.playRound(1, owner, opponent);

        assertEquals(1, berserker.getRoundsInPlay());
        assertEquals(0, berserker.getAttackBonus());

        roundManager.playRound(2, owner, opponent);

        assertEquals(2, berserker.getRoundsInPlay());
        assertEquals(2, berserker.getAttackBonus());

        // One DEF is lost to the effect and one per combat.
        assertEquals(5, berserker.getCurrentDefense());
    }

    @Test
    void shouldResumeWhenDefenseBecomesAvailable() {

        Card berserker = createCard(
                "berserker", 2, 3, CardEffect.BERSERKER);

        Player owner = createPlayer("p1", berserker);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        BerserkerEffect effect = new BerserkerEffect();

        EffectContext context = new EffectContext(
                berserker, owner, opponent);

        // First round: no activation.
        berserker.incrementRoundsInPlay();
        effect.onRoundStart(context);

        assertEquals(3, berserker.getCurrentDefense());
        assertEquals(0, berserker.getAttackBonus());

        // Second round: pay 1 DEF and gain +2 ATK.
        berserker.incrementRoundsInPlay();
        effect.onRoundStart(context);

        assertEquals(2, berserker.getCurrentDefense());
        assertEquals(2, berserker.getAttackBonus());

        // Simulate losing the remaining available DEF.
        berserker.reduceCurrentDefense(1);

        // Third round: cannot pay.
        berserker.incrementRoundsInPlay();
        effect.onRoundStart(context);

        assertEquals(1, berserker.getCurrentDefense());
        assertEquals(2, berserker.getAttackBonus());

        // Simulate a future effect restoring current DEF.
        berserker.increaseMaxDefense(1);

        assertEquals(2, berserker.getCurrentDefense());

        // Fourth round: Berserker resumes.
        berserker.incrementRoundsInPlay();
        effect.onRoundStart(context);

        assertEquals(1, berserker.getCurrentDefense());
        assertEquals(4, berserker.getAttackBonus());

        // Fifth round: maximum bonus already reached.
        berserker.incrementRoundsInPlay();
        effect.onRoundStart(context);

        assertEquals(1, berserker.getCurrentDefense());
        assertEquals(4, berserker.getAttackBonus());
    }

}
