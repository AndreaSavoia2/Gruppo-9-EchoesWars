
package com.echoeswars.effects;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.combat.CombatResult;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AmbushEffectTest {

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
    void shouldGainTwoAttackOnEntry() {

        Card ambush = createCard(
                "ambush", 3, 5, CardEffect.IMBOSCATA);

        Player owner = createPlayer("p1", ambush);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        new EffectResolver().resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(3, ambush.getAttack());
        assertEquals(2, ambush.getTemporaryAttackBonus());
        assertEquals(5, ambush.getBaseEffectiveAttack());
    }

    @Test
    void shouldUseBonusDuringFirstCombat() {

        Card ambush = createCard(
                "ambush", 3, 5, CardEffect.IMBOSCATA);

        Player owner = createPlayer("p1", ambush);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        CombatResult result = new CombatResolver().resolve(owner, opponent);

        // The first result belongs to the first player.
        assertEquals(
                5,
                result.getCards().get(0).getAttack());
    }

    @Test
    void shouldExpireAtEndOfEntryRound() {

        Card ambush = createCard(
                "ambush", 3, 5, CardEffect.IMBOSCATA);

        Player owner = createPlayer("p1", ambush);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(5, ambush.getBaseEffectiveAttack());

        resolver.resolveRoundEndEffects(
                owner, opponent, true, false);

        assertEquals(0, ambush.getTemporaryAttackBonus());
        assertEquals(3, ambush.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotRegainBonusInLaterRounds() {

        Card ambush = createCard(
                "ambush", 3, 5, CardEffect.IMBOSCATA);

        Player owner = createPlayer("p1", ambush);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        resolver.resolveRoundEndEffects(
                owner, opponent, true, false);

        resolver.resolveEntryEffects(
                owner, opponent, false, false);

        resolver.resolveRoundEndEffects(
                owner, opponent, false, false);

        assertEquals(0, ambush.getTemporaryAttackBonus());
        assertEquals(3, ambush.getBaseEffectiveAttack());
    }

    @Test
    void shouldPreservePermanentAttackBonuses() {

        Card ambush = createCard(
                "ambush", 3, 5, CardEffect.IMBOSCATA);

        Player owner = createPlayer("p1", ambush);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        ambush.addAttackBonus(2);

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(7, ambush.getBaseEffectiveAttack());

        resolver.resolveRoundEndEffects(
                owner, opponent, true, false);

        assertEquals(2, ambush.getAttackBonus());
        assertEquals(0, ambush.getTemporaryAttackBonus());
        assertEquals(5, ambush.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotChangePrintedAttack() {

        Card ambush = createCard(
                "ambush", 3, 5, CardEffect.IMBOSCATA);

        Player owner = createPlayer("p1", ambush);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        new EffectResolver().resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(3, ambush.getAttack());
        assertEquals(5, ambush.getBaseEffectiveAttack());
    }

    @Test
    void shouldNotAffectOtherCards() {

        Card ordinary = createCard(
                "ordinary", 3, 5, CardEffect.NONE);

        Player owner = createPlayer("p1", ordinary);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 8, CardEffect.NONE));

        new EffectResolver().resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(0, ordinary.getTemporaryAttackBonus());
        assertEquals(3, ordinary.getBaseEffectiveAttack());
    }
}
