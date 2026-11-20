
package com.echoeswars.effects;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DisciplineEffectTest {

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

    private Player createPlayer(
            String id,
            Card activeCard) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(activeCard);

        Player player = new Player(id, deck);
        player.drawCard();

        return player;
    }

    private void resolveEntry(
            Player player1,
            Player player2) {

        new EffectResolver().resolveEntryEffects(
                player1,
                player2,
                true,
                false);
    }

    @Test
    void shouldIncreaseDefenseAfterWarriorDestroyed() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Player player1 = createPlayer("p1", discipline);

        Card destroyedWarrior = createCard(
                "warrior",
                CardSchool.GUERRIERI,
                CardEffect.NONE);

        player1.registerDestroyedCard(destroyedWarrior);

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        resolveEntry(player1, player2);

        assertEquals(6, discipline.getCurrentDefense());
        assertEquals(6, discipline.getMaxDefense());

        // Printed defense never changes.
        assertEquals(5, discipline.getDefense());
    }

    @Test
    void shouldNotActivateAfterMageDestroyed() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Player player1 = createPlayer("p1", discipline);

        player1.registerDestroyedCard(
                createCard(
                        "mage",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(player1, player2);

        assertEquals(5, discipline.getCurrentDefense());
        assertEquals(5, discipline.getMaxDefense());
    }

    @Test
    void shouldNotActivateAfterRogueDestroyed() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Player player1 = createPlayer("p1", discipline);

        player1.registerDestroyedCard(
                createCard(
                        "rogue",
                        CardSchool.FURFANTI,
                        CardEffect.NONE));

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        resolveEntry(player1, player2);

        assertEquals(5, discipline.getCurrentDefense());
    }

    @Test
    void shouldNotActivateWithoutPreviousDestruction() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Player player1 = createPlayer("p1", discipline);

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        resolveEntry(player1, player2);

        assertNull(player1.getLastDestroyedSchool());
        assertEquals(5, discipline.getCurrentDefense());
    }

    @Test
    void shouldIgnoreCardsDiscardedFromDeck() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Card discardedWarrior = createCard(
                "discarded-warrior",
                CardSchool.GUERRIERI,
                CardEffect.NONE);

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(discipline);
        deck.add(discardedWarrior);

        Player player1 = new Player("p1", deck);
        player1.drawCard();

        // A direct discard does not count as field destruction.
        player1.discardTopCard();

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        resolveEntry(player1, player2);

        assertEquals(1, player1.getDiscardPileSize());
        assertNull(player1.getLastDestroyedSchool());
        assertEquals(5, discipline.getCurrentDefense());
    }

    @Test
    void shouldNotApplyBonusAgainInLaterRounds() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Player player1 = createPlayer("p1", discipline);

        player1.registerDestroyedCard(
                createCard(
                        "warrior",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        EffectResolver resolver = new EffectResolver();

        // Discipline enters.
        resolver.resolveEntryEffects(
                player1,
                player2,
                true,
                false);

        assertEquals(6, discipline.getCurrentDefense());

        // Neither card enters in the next round.
        resolver.resolveEntryEffects(
                player1,
                player2,
                false,
                false);

        assertEquals(6, discipline.getCurrentDefense());
        assertEquals(6, discipline.getMaxDefense());
    }

    @Test
    void shouldUseMostRecentlyDestroyedSchool() {

        Card discipline = createCard(
                "discipline",
                CardSchool.GUERRIERI,
                CardEffect.DISCIPLINA);

        Player player1 = createPlayer("p1", discipline);

        player1.registerDestroyedCard(
                createCard(
                        "warrior",
                        CardSchool.GUERRIERI,
                        CardEffect.NONE));

        player1.registerDestroyedCard(
                createCard(
                        "mage",
                        CardSchool.MAGHI,
                        CardEffect.NONE));

        Player player2 = createPlayer(
                "p2",
                createCard(
                        "opponent",
                        CardSchool.FURFANTI,
                        CardEffect.NONE));

        resolveEntry(player1, player2);

        // The Mage was destroyed after the Warrior.
        assertEquals(
                CardSchool.MAGHI,
                player1.getLastDestroyedSchool());

        assertEquals(5, discipline.getCurrentDefense());
    }
}
