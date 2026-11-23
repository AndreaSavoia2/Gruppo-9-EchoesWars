
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

class PiercingEffectTest {

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

    private void playRound(Player first, Player second) {

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, first, second);
    }

    @Test
    void shouldIgnoreResistanceDamageReduction() {

        Card piercing = createCard(
                "piercing", 3, 10, CardEffect.PERFORANTE);

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        playRound(
                createPlayer("p1", piercing),
                createPlayer("p2", resistant));

        // Piercing deals all 3 damage instead of 2.
        assertEquals(7, resistant.getCurrentDefense());
    }

    @Test
    void shouldReduceNormalAttackAgainstResistance() {

        Card normal = createCard(
                "normal", 3, 10, CardEffect.NONE);

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        playRound(
                createPlayer("p1", normal),
                createPlayer("p2", resistant));

        // Resistance reduces 3 damage to 2.
        assertEquals(8, resistant.getCurrentDefense());
    }

    @Test
    void shouldNotIgnoreProtection() {

        Card piercing = createCard(
                "piercing", 3, 10, CardEffect.PERFORANTE);

        Card guardian = createCard(
                "guardian", 1, 10, CardEffect.GUARDIANO);

        playRound(
                createPlayer("p1", piercing),
                createPlayer("p2", guardian));

        // Guardian absorbs 2 damage with Protection.
        // Only 1 damage reaches DEF.
        assertEquals(9, guardian.getCurrentDefense());
    }

    @Test
    void shouldNotModifyAttackValue() {

        Card piercing = createCard(
                "piercing", 3, 10, CardEffect.PERFORANTE);

        Card normal = createCard(
                "normal", 1, 10, CardEffect.NONE);

        playRound(
                createPlayer("p1", piercing),
                createPlayer("p2", normal));

        assertEquals(3, piercing.getBaseEffectiveAttack());
        assertEquals(7, normal.getCurrentDefense());
    }

    @Test
    void shouldWorkWithEchoBonus() {

        Card piercing = new Card(
                "piercing",
                "piercing",
                3,
                10,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                CardEffect.PERFORANTE);

        Card resistant = new Card(
                "resistant",
                "resistant",
                1,
                10,
                1,
                Echo.SAPIENZA,
                CardSchool.GUERRIERI,
                CardEffect.RESISTENZA);

        playRound(
                createPlayer("p1", piercing),
                createPlayer("p2", resistant));

        // DOMINIO defeats SAPIENZA in the Echo cycle.
        // Attack 3 + Echo bonus 1 = 4.
        // Piercing ignores Resistance.
        assertEquals(6, resistant.getCurrentDefense());
    }

    @Test
    void shouldNotAffectReflectionDamage() {

        Card piercing = createCard(
                "piercing", 1, 5, CardEffect.PERFORANTE);

        Card reflector = createCard(
                "reflector", 1, 10, CardEffect.RIFLESSO);

        playRound(
                createPlayer("p1", piercing),
                createPlayer("p2", reflector));

        // Reflection still deals exactly 1 special damage.
        // Enemy attack deals another 1 damage.
        assertEquals(3, piercing.getCurrentDefense());
    }

    @Test
    void shouldNotChangeNormalCombat() {

        Card piercing = createCard(
                "piercing", 2, 10, CardEffect.PERFORANTE);

        Card normal = createCard(
                "normal", 2, 10, CardEffect.NONE);

        playRound(
                createPlayer("p1", piercing),
                createPlayer("p2", normal));

        assertEquals(8, piercing.getCurrentDefense());
        assertEquals(8, normal.getCurrentDefense());
    }
}
