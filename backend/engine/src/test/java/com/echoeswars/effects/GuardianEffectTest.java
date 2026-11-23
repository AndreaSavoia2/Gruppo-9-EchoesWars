
package com.echoeswars.effects;

import com.echoeswars.combat.CombatResolver;
import com.echoeswars.effects.handlers.GuardianEffect;
import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GuardianEffectTest {

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
    void shouldGainTwoProtectionOnEntry() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        new EffectResolver().resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(2, guardian.getProtection());
        assertEquals(5, guardian.getCurrentDefense());
        assertEquals(5, guardian.getMaxDefense());
    }

    @Test
    void shouldAbsorbDamageBeforeDefense() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        new CombatResolver().resolve(owner, opponent);

        assertEquals(1, guardian.getProtection());
        assertEquals(5, guardian.getCurrentDefense());
    }

    @Test
    void shouldApplyRemainingDamageToDefense() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 3, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        new CombatResolver().resolve(owner, opponent);

        assertEquals(0, guardian.getProtection());
        assertEquals(4, guardian.getCurrentDefense());
    }

    @Test
    void shouldExpireUnusedProtectionAtRoundEnd() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(2, guardian.getProtection());

        resolver.resolveRoundEndEffects(
                owner, opponent, true, false);

        assertEquals(0, guardian.getProtection());
        assertEquals(5, guardian.getCurrentDefense());
    }

    @Test
    void shouldNotRegainProtectionInLaterRounds() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        resolver.resolveRoundEndEffects(
                owner, opponent, true, false);

        // The same card remains in play.
        resolver.resolveEntryEffects(
                owner, opponent, false, false);

        resolver.resolveRoundEndEffects(
                owner, opponent, false, false);

        assertEquals(0, guardian.getProtection());
    }

    @Test
    void shouldNotRemoveProtectionBeforeRoundEnd() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(2, guardian.getProtection());

        // Combat consumes one point of protection.
        new CombatResolver().resolve(owner, opponent);

        assertEquals(1, guardian.getProtection());

        // Only the unused point expires.
        resolver.resolveRoundEndEffects(
                owner, opponent, true, false);

        assertEquals(0, guardian.getProtection());
    }

    @Test
    void shouldNotChangePrintedDefense() {

        Card guardian = createCard(
                "guardian", 2, 5, CardEffect.GUARDIANO);

        Player owner = createPlayer("p1", guardian);

        Player opponent = createPlayer(
                "p2",
                createCard("enemy", 1, 10, CardEffect.NONE));

        new GuardianEffect().onEntry(
                new EffectContext(guardian, owner, opponent));

        assertEquals(5, guardian.getDefense());
        assertEquals(5, guardian.getMaxDefense());
        assertEquals(2, guardian.getProtection());
    }
}
