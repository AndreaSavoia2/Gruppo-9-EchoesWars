
package com.echoeswars.combat;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionEffectTest {

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
    void shouldDealOneSpecialDamageToAttacker() {

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 2, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        // Attacker loses 1 DEF to ordinary combat
        // and 1 additional DEF to Reflection.
        assertEquals(8, attacker.getCurrentDefense());
        assertEquals(8, reflector.getCurrentDefense());
    }

    @Test
    void shouldTriggerWhenProtectionAbsorbsEntireAttack() {

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 1, 10, CardEffect.NONE);

        reflector.addProtection(2);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        assertEquals(10, reflector.getCurrentDefense());
        assertEquals(1, reflector.getProtection());

        // Ordinary attack 1 + Reflection 1.
        assertEquals(8, attacker.getCurrentDefense());
    }

    @Test
    void shouldTriggerEvenWhenReflectorIsDestroyed() {

        Card reflector = createCard(
                "reflector", 1, 2, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 3, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        assertTrue(reflector.isDestroyed());

        // Attacker receives ordinary attack and Reflection.
        assertEquals(8, attacker.getCurrentDefense());
    }

    @Test
    void shouldBeAbsorbedByProtection() {

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 2, 10, CardEffect.NONE);

        attacker.addProtection(2);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        // Protection absorbs the ordinary attack
        // and the reflected special damage.
        assertEquals(10, attacker.getCurrentDefense());
        assertEquals(0, attacker.getProtection());
    }

    @Test
    void shouldNotCauseReflectionChains() {

        Card first = createCard(
                "first", 1, 10, CardEffect.RIFLESSO);

        Card second = createCard(
                "second", 1, 10, CardEffect.RIFLESSO);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        new CombatResolver().resolve(player1, player2);

        // Each card receives 1 ordinary damage
        // and 1 Reflection damage.
        assertEquals(8, first.getCurrentDefense());
        assertEquals(8, second.getCurrentDefense());
    }

    @Test
    void shouldIgnoreResistanceForSpecialDamage() {

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        Card resistant = createCard(
                "resistant", 2, 10, CardEffect.RESISTENZA);

        resistant.incrementRoundsInPlay();

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", resistant);

        new CombatResolver().resolve(owner, opponent);

        // Ordinary attack 1 stays at minimum 1.
        // Reflection deals 1 additional special damage.
        assertEquals(8, resistant.getCurrentDefense());
    }

    @Test
    void shouldNotDealOverflowFromReflectionAlone() {

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 1, 2, CardEffect.NONE);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        assertTrue(attacker.isDestroyed());

        // Reflection brings DEF exactly to zero,
        // so no overflow damage is dealt.
        assertEquals(10, opponent.getHealth());
    }

    @Test
    void shouldNotModifyPrintedAttack() {

        Card reflector = createCard(
                "reflector", 2, 10, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 1, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        CombatResult result = new CombatResolver().resolve(owner, opponent);

        assertEquals(2, reflector.getAttack());
        assertEquals(2, result.getCards().get(0).getAttack());

        // Attacker receives 2 ordinary damage
        // plus 1 reflected damage.
        assertEquals(7, attacker.getCurrentDefense());
    }

    @Test
    void shouldExcludeReflectionFromOrdinaryAttackOverflow() {

        Card reflector = createCard(
                "reflector", 3, 10, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 1, 2, CardEffect.NONE);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        assertTrue(attacker.isDestroyed());

        // Ordinary attack: 3 damage against 2 DEF = 1 overflow.
        // Reflection: 1 special damage, excluded from overflow.
        assertEquals(9, opponent.getHealth());
    }

    @Test
    void shouldNotCauseOverflowWhenBothCardsAreDestroyed() {

        Card reflector = createCard(
                "reflector", 3, 2, CardEffect.RIFLESSO);

        Card attacker = createCard(
                "attacker", 3, 2, CardEffect.NONE);

        Player owner = createPlayer("p1", reflector);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        assertTrue(reflector.isDestroyed());
        assertTrue(attacker.isDestroyed());

        // Simultaneous destruction does not cause HP overflow.
        assertEquals(10, owner.getHealth());
        assertEquals(10, opponent.getHealth());
    }

}
