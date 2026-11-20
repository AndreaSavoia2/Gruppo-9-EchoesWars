
package com.echoeswars.effects;

import com.echoeswars.game.RoundManager;
import com.echoeswars.combat.CombatResolver;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class HealingEffectTest {

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

    private void resolveRoundEnd(
            EffectResolver resolver,
            Player owner,
            Player opponent,
            boolean ownerEntered) {

        resolver.resolveRoundEndEffects(
                owner,
                opponent,
                ownerEntered,
                false);
    }

    @Test
    void shouldHealOneHpAtRoundEnd() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(3);

        resolveRoundEnd(
                new EffectResolver(),
                owner,
                opponent,
                true);

        assertEquals(8, owner.getHealth());
        assertEquals(1, healingCard.getHealingDone());
    }

    @Test
    void shouldHealInLaterRounds() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(5);

        EffectResolver resolver = new EffectResolver();

        resolveRoundEnd(resolver, owner, opponent, true);
        resolveRoundEnd(resolver, owner, opponent, false);

        assertEquals(7, owner.getHealth());
        assertEquals(2, healingCard.getHealingDone());
    }

    @Test
    void shouldStopAfterThreeActualHpRestored() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(5);

        EffectResolver resolver = new EffectResolver();

        for (int i = 0; i < 5; i++) {
            resolveRoundEnd(
                    resolver,
                    owner,
                    opponent,
                    i == 0);
        }

        assertEquals(8, owner.getHealth());
        assertEquals(3, healingCard.getHealingDone());
    }

    @Test
    void shouldNotExceedTwentyHp() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.heal(10);

        resolveRoundEnd(
                new EffectResolver(),
                owner,
                opponent,
                true);

        assertEquals(20, owner.getHealth());
        assertEquals(0, healingCard.getHealingDone());
    }

    @Test
    void shouldNotConsumeLimitAtFullHealth() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        // Bring the player to the maximum of 20 HP.
        owner.heal(10);

        assertEquals(20, owner.getHealth());

        // Healing at full HP must not consume the card's limit.
        resolveRoundEnd(resolver, owner, opponent, true);

        assertEquals(20, owner.getHealth());
        assertEquals(0, healingCard.getHealingDone());

        // After losing HP, healing must work normally.
        owner.takeDamage(2);

        resolveRoundEnd(resolver, owner, opponent, false);

        assertEquals(19, owner.getHealth());
        assertEquals(1, healingCard.getHealingDone());
    }

    @Test
    void shouldNotHealWhenCardIsDestroyed() {

        Card healingCard = createCard(
                "healing", 1, 2, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(3);
        healingCard.takeDamage(2);

        resolveRoundEnd(
                new EffectResolver(),
                owner,
                opponent,
                true);

        assertEquals(7, owner.getHealth());
        assertEquals(0, healingCard.getHealingDone());
    }

    @Test
    void shouldNotRevivePlayerAtZeroHp() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Player owner = createPlayer("p1", healingCard);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        owner.takeDamage(10);

        resolveRoundEnd(
                new EffectResolver(),
                owner,
                opponent,
                true);

        assertEquals(0, owner.getHealth());
        assertEquals(0, healingCard.getHealingDone());
    }

    @Test
    void shouldPreserveGuardianExpiration() {

        Card guardian = createCard(
                "guardian", 1, 10, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(2, guardian.getProtection());

        resolveRoundEnd(resolver, owner, opponent, true);

        assertEquals(0, guardian.getProtection());
    }

    @Test
    void shouldNotHealAfterCombatHpDefeat() {

        Card healingCard = createCard(
                "healing", 1, 10, CardEffect.GUARIGIONE);

        Card enemyCard = createCard(
                "enemy", 10, 1, CardEffect.NONE);

        Player owner = createPlayer("p1", healingCard);
        Player opponent = createPlayer("p2", enemyCard);

        // Reduce the owner to 1 HP before combat.
        owner.takeDamage(9);

        RoundManager roundManager = new RoundManager(
                new CombatResolver(),
                new EffectResolver());

        roundManager.playRound(1, owner, opponent);

        // The enemy survives because its attack is simultaneous,
        // but its card is destroyed by the healing card.
        // The healing card survives at DEF 0? No:
        // the enemy attack destroys it, so no overflow
        // occurs in this setup.
        assertEquals(1, owner.getHealth());
        assertEquals(0, healingCard.getHealingDone());
    }
}
