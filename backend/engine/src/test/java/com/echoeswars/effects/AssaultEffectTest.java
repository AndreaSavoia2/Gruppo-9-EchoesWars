
package com.echoeswars.effects;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.effects.handlers.AssaultEffect;
import com.echoeswars.game.RoundManager;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AssaultEffectTest {

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

        new AssaultEffect().onRoundStart(
                new EffectContext(card, owner, opponent));
    }

    @Test
    void shouldNotGainAttackDuringFirstRound() {

        Card assault = createCard(
                "assault", 2, 8, CardEffect.ASSALTO);

        Player owner = createPlayer("p1", assault);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        startRound(assault, owner, opponent);

        assertEquals(1, assault.getRoundsInPlay());
        assertEquals(0, assault.getAttackBonus());
        assertEquals(2, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldGainOneAttackDuringSecondRound() {

        Card assault = createCard(
                "assault", 2, 8, CardEffect.ASSALTO);

        Player owner = createPlayer("p1", assault);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        startRound(assault, owner, opponent);
        startRound(assault, owner, opponent);

        assertEquals(2, assault.getRoundsInPlay());
        assertEquals(1, assault.getAttackBonus());
        assertEquals(3, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldReachMaximumBonusDuringFourthRound() {

        Card assault = createCard(
                "assault", 2, 8, CardEffect.ASSALTO);

        Player owner = createPlayer("p1", assault);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        for (int i = 0; i < 4; i++) {
            startRound(assault, owner, opponent);
        }

        assertEquals(4, assault.getRoundsInPlay());
        assertEquals(3, assault.getAttackBonus());
        assertEquals(5, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotExceedMaximumBonus() {

        Card assault = createCard(
                "assault", 2, 8, CardEffect.ASSALTO);

        Player owner = createPlayer("p1", assault);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        for (int i = 0; i < 10; i++) {
            startRound(assault, owner, opponent);
        }

        assertEquals(10, assault.getRoundsInPlay());
        assertEquals(3, assault.getAttackBonus());
        assertEquals(5, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldPreserveExistingPermanentAttackBonus() {

        Card assault = createCard(
                "assault", 2, 8, CardEffect.ASSALTO);

        Player owner = createPlayer("p1", assault);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        assault.addAttackBonus(2);

        startRound(assault, owner, opponent);
        startRound(assault, owner, opponent);

        assertEquals(3, assault.getAttackBonus());
        assertEquals(5, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotChangePrintedAttack() {

        Card assault = createCard(
                "assault", 2, 8, CardEffect.ASSALTO);

        Player owner = createPlayer("p1", assault);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        for (int i = 0; i < 4; i++) {
            startRound(assault, owner, opponent);
        }

        assertEquals(2, assault.getAttack());
        assertEquals(5, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldApplyBonusInRealRoundManager() {

        Card assault = createCard(
                "assault", 1, 8, CardEffect.ASSALTO);

        Card enemy = createCard(
                "enemy", 1, 8, CardEffect.NONE);

        Player owner = createPlayer("p1", assault);
        Player opponent = createPlayer("p2", enemy);

        RoundManager roundManager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        roundManager.playRound(1, owner, opponent);

        assertEquals(1, assault.getRoundsInPlay());
        assertEquals(0, assault.getAttackBonus());

        roundManager.playRound(2, owner, opponent);

        assertEquals(2, assault.getRoundsInPlay());
        assertEquals(1, assault.getAttackBonus());
        assertEquals(2, assault.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotApplyAssaultToOtherEffects() {

        Card ordinary = createCard(
                "ordinary", 2, 8, CardEffect.NONE);

        Player owner = createPlayer("p1", ordinary);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        for (int i = 0; i < 4; i++) {

            ordinary.incrementRoundsInPlay();

            resolver.resolveRoundStartEffects(
                    owner,
                    opponent);
        }

        assertEquals(0, ordinary.getAttackBonus());
        assertEquals(2, ordinary.getBaseEffectiveAttack());
    }
}
