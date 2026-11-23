
package com.echoeswars.effects;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ArcanePreparationEffectTest {

    private Card createCard(
            String id,
            CardSchool school,
            CardEffect effect) {

        return new Card(
                id,
                id,
                2,
                5,
                1,
                Echo.DOMINIO,
                school,
                effect);
    }

    private Player createPlayer(String id, Card card) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(card);

        Player player = new Player(id, deck);
        player.drawCard();

        return player;
    }

    private void resolveEntry(Player owner, Player opponent) {

        new EffectResolver().resolveEntryEffects(
                owner,
                opponent,
                true,
                false);
    }

    @Test
    void shouldGainAttackAfterMageDestroyed() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Player owner = createPlayer("p1", arcane);

        owner.registerDestroyedCard(
                createCard(
                        "destroyed-mage",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(owner, opponent);

        assertEquals(1, arcane.getAttackBonus());
        assertEquals(3, arcane.getBaseEffectiveAttack());
        assertEquals(2, arcane.getAttack());
    }

    @Test
    void shouldNotActivateAfterWarriorDestroyed() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Player owner = createPlayer("p1", arcane);

        owner.registerDestroyedCard(
                createCard(
                        "warrior",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(owner, opponent);

        assertEquals(0, arcane.getAttackBonus());
    }

    @Test
    void shouldNotActivateAfterRogueDestroyed() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Player owner = createPlayer("p1", arcane);

        owner.registerDestroyedCard(
                createCard(
                        "rogue",
                        CardSchool.FURFANTI,
                        CardEffect.NONE));

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(owner, opponent);

        assertEquals(0, arcane.getAttackBonus());
    }

    @Test
    void shouldNotActivateWithoutPreviousDestruction() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Player owner = createPlayer("p1", arcane);

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(owner, opponent);

        assertNull(owner.getLastDestroyedSchool());
        assertEquals(0, arcane.getAttackBonus());
    }

    @Test
    void shouldIgnoreDirectDeckDiscards() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Card discardedMage = createCard(
                "discarded-mage",
                CardSchool.MAGHI,
                CardEffect.NONE);

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(arcane);
        deck.add(discardedMage);

        Player owner = new Player("p1", deck);
        owner.drawCard();
        owner.discardTopCard();

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(owner, opponent);

        assertEquals(1, owner.getDiscardPileSize());
        assertNull(owner.getLastDestroyedSchool());
        assertEquals(0, arcane.getAttackBonus());
    }

    @Test
    void shouldNotGainBonusAgainNextRound() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Player owner = createPlayer("p1", arcane);

        owner.registerDestroyedCard(
                createCard(
                        "mage",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        resolver.resolveEntryEffects(
                owner, opponent, true, false);

        assertEquals(1, arcane.getAttackBonus());

        // No new entry occurs in the following round.
        resolver.resolveEntryEffects(
                owner, opponent, false, false);

        assertEquals(1, arcane.getAttackBonus());
    }

    @Test
    void shouldUseMostRecentlyDestroyedSchool() {

        Card arcane = createCard(
                "arcane",
                CardSchool.MAGHI,
                CardEffect.PREPARAZIONE_ARCANA);

        Player owner = createPlayer("p1", arcane);

        owner.registerDestroyedCard(
                createCard(
                        "mage",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        owner.registerDestroyedCard(
                createCard(
                        "warrior",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        Player opponent = createPlayer(
                "p2",
                createCard(
                        "enemy",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(owner, opponent);

        assertEquals(
                CardSchool.GUERRIERI,
                owner.getLastDestroyedSchool());

        assertEquals(0, arcane.getAttackBonus());
    }
}
