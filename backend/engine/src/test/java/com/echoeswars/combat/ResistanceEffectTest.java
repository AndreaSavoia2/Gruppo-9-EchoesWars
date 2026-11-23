
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResistanceEffectTest {

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
    void shouldReduceDamageDuringFirstRound() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 4, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        resistant.incrementRoundsInPlay();

        new CombatResolver().resolve(owner, opponent);

        assertEquals(7, resistant.getCurrentDefense());
    }

    @Test
    void shouldReduceDamageDuringSecondRound() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 4, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        resistant.incrementRoundsInPlay();
        resistant.incrementRoundsInPlay();

        new CombatResolver().resolve(owner, opponent);

        assertEquals(7, resistant.getCurrentDefense());
    }

    @Test
    void shouldNotReduceDamageFromThirdRound() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 4, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        for (int i = 0; i < 3; i++) {
            resistant.incrementRoundsInPlay();
        }

        new CombatResolver().resolve(owner, opponent);

        assertEquals(6, resistant.getCurrentDefense());
    }

    @Test
    void shouldNeverReduceDamageBelowOne() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 1, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        resistant.incrementRoundsInPlay();

        new CombatResolver().resolve(owner, opponent);

        assertEquals(9, resistant.getCurrentDefense());
    }

    @Test
    void shouldNotModifyAttackerAttack() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 4, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        resistant.incrementRoundsInPlay();

        CombatResult result = new CombatResolver().resolve(owner, opponent);

        assertEquals(
                4,
                result.getCards().get(1).getAttack());

        assertEquals(7, resistant.getCurrentDefense());
    }

    @Test
    void shouldReduceDamageBeforeProtection() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 4, 10, CardEffect.NONE);

        resistant.addProtection(2);
        resistant.incrementRoundsInPlay();

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        // Incoming 4 -> Resistance reduces to 3.
        // Protection absorbs 2, DEF loses 1.
        assertEquals(9, resistant.getCurrentDefense());
        assertEquals(0, resistant.getProtection());
    }

    @Test
    void shouldNotApplyToCardsWithoutResistance() {

        Card ordinary = createCard(
                "ordinary", 1, 10, CardEffect.NONE);

        Card attacker = createCard(
                "attacker", 4, 10, CardEffect.NONE);

        ordinary.incrementRoundsInPlay();

        Player owner = createPlayer("p1", ordinary);
        Player opponent = createPlayer("p2", attacker);

        new CombatResolver().resolve(owner, opponent);

        assertEquals(6, ordinary.getCurrentDefense());
    }

    @Test
    void shouldWorkThroughRoundManager() {

        Card resistant = createCard(
                "resistant", 1, 10, CardEffect.RESISTENZA);

        Card attacker = createCard(
                "attacker", 2, 10, CardEffect.NONE);

        Player owner = createPlayer("p1", resistant);
        Player opponent = createPlayer("p2", attacker);

        RoundManager manager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        manager.playRound(1, owner, opponent);

        assertEquals(1, resistant.getRoundsInPlay());
        assertEquals(9, resistant.getCurrentDefense());

        manager.playRound(2, owner, opponent);

        assertEquals(2, resistant.getRoundsInPlay());
        assertEquals(8, resistant.getCurrentDefense());

        manager.playRound(3, owner, opponent);

        assertEquals(3, resistant.getRoundsInPlay());
        assertEquals(6, resistant.getCurrentDefense());

    }

    @Test
    void shouldRecordProtectionConsumedByReflection() {

        Card firstCard = new Card(
                "first",
                "C001",
                2,
                5,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                CardEffect.RIFLESSO);

        Card secondCard = new Card(
                "second",
                "C002",
                2,
                5,
                1,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                CardEffect.RIFLESSO);

        firstCard.addProtection(3);
        secondCard.addProtection(3);

        Player firstPlayer = new Player(
                "player-1",
                new ArrayList<>(List.of(firstCard)));

        Player secondPlayer = new Player(
                "player-2",
                new ArrayList<>(List.of(secondCard)));

        firstPlayer.drawCard();
        secondPlayer.drawCard();

        CombatResult result = new CombatResolver()
                .resolve(firstPlayer, secondPlayer);

        CombatCardResult firstResult = result.getCards().get(0);

        CombatCardResult secondResult = result.getCards().get(1);

        assertEquals(3, firstResult.getProtectionBefore());
        assertEquals(0, firstResult.getProtectionAfter());

        assertEquals(3, secondResult.getProtectionBefore());
        assertEquals(0, secondResult.getProtectionAfter());

        assertEquals(5, firstResult.getDefenseAfter());
        assertEquals(5, secondResult.getDefenseAfter());
    }

}
