
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

class DuelistEffectTest {

    private Card createCard(
            String id,
            int attack,
            int defense,
            CardSchool school,
            CardEffect effect) {

        return new Card(
                id,
                id,
                attack,
                defense,
                1,
                Echo.DOMINIO,
                school,
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
    void shouldGainOneAttackAgainstMage() {

        Card duelist = createCard(
                "duelist", 2, 10,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card mage = createCard(
                "mage", 1, 10,
                CardSchool.MAGHI,
                CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", duelist),
                createPlayer("p2", mage));

        // Duelist deals 2 base ATK + 1 bonus.
        assertEquals(7, mage.getCurrentDefense());
    }

    @Test
    void shouldNotGainBonusAgainstWarrior() {

        Card duelist = createCard(
                "duelist", 2, 10,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card warrior = createCard(
                "warrior", 1, 10,
                CardSchool.GUERRIERI,
                CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", duelist),
                createPlayer("p2", warrior));

        assertEquals(8, warrior.getCurrentDefense());
    }

    @Test
    void shouldApplyBonusOnFirstRound() {

        Card duelist = createCard(
                "duelist", 1, 10,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card mage = createCard(
                "mage", 1, 10,
                CardSchool.MAGHI,
                CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", duelist),
                createPlayer("p2", mage));

        assertEquals(8, mage.getCurrentDefense());
    }

    @Test
    void shouldNotAccumulateBonusAcrossRounds() {

        Card duelist = createCard(
                "duelist", 1, 20,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card mage = createCard(
                "mage", 1, 20,
                CardSchool.MAGHI,
                CardEffect.NONE);

        Player owner = createPlayer("p1", duelist);
        Player opponent = createPlayer("p2", mage);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);
        assertEquals(18, mage.getCurrentDefense());

        manager.playRound(2, owner, opponent);
        assertEquals(16, mage.getCurrentDefense());

        manager.playRound(3, owner, opponent);
        assertEquals(14, mage.getCurrentDefense());

        assertEquals(1, duelist.getBaseEffectiveAttack());
    }

    @Test
    void shouldLoseBonusWhenOpponentChangesToWarrior() {

        Card duelist = createCard(
                "duelist", 2, 20,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card mage = createCard(
                "mage", 1, 2,
                CardSchool.MAGHI,
                CardEffect.NONE);

        Card warrior = createCard(
                "warrior", 1, 10,
                CardSchool.GUERRIERI,
                CardEffect.NONE);

        Player owner = createPlayer("p1", duelist);
        Player opponent = createPlayer(
                "p2", mage, warrior);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);

        assertTrue(mage.isDestroyed());

        // The replacement is a Warrior.
        assertSame(warrior, opponent.getActiveCard());

        manager.playRound(2, owner, opponent);

        // Duelist now deals only its base 2 ATK.
        assertEquals(8, warrior.getCurrentDefense());
    }

    @Test
    void shouldGainBonusWhenOpponentChangesToMage() {

        Card duelist = createCard(
                "duelist", 2, 20,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card warrior = createCard(
                "warrior", 1, 2,
                CardSchool.GUERRIERI,
                CardEffect.NONE);

        Card mage = createCard(
                "mage", 1, 10,
                CardSchool.MAGHI,
                CardEffect.NONE);

        Player owner = createPlayer("p1", duelist);
        Player opponent = createPlayer(
                "p2", warrior, mage);

        RoundManager manager = createManager();

        manager.playRound(1, owner, opponent);

        assertTrue(warrior.isDestroyed());
        assertSame(mage, opponent.getActiveCard());

        manager.playRound(2, owner, opponent);

        // Duelist gains +1 ATK against the new Mage.
        assertEquals(7, mage.getCurrentDefense());
    }

    @Test
    void shouldNotModifyPermanentAttackBonus() {

        Card duelist = createCard(
                "duelist", 2, 10,
                CardSchool.GUERRIERI,
                CardEffect.DUELLANTE);

        Card mage = createCard(
                "mage", 1, 10,
                CardSchool.MAGHI,
                CardEffect.NONE);

        createManager().playRound(
                1,
                createPlayer("p1", duelist),
                createPlayer("p2", mage));

        assertEquals(0, duelist.getAttackBonus());
        assertEquals(0, duelist.getTemporaryAttackBonus());
        assertEquals(2, duelist.getBaseEffectiveAttack());
    }
}
