package com.echoeswars.combat;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CombatResolverTest {

    private final CombatResolver resolver = new CombatResolver();

    private Card createCard(
            String instanceId,
            int attack,
            int defense,
            Echo echo) {

        return new Card(
                instanceId,
                "C001",
                attack,
                defense,
                1,
                echo,
                CardSchool.GUERRIERI,
                CardEffect.NONE
        );
    }

    private Player createPlayer(String id, Card card) {

        ArrayList<Card> deck = new ArrayList<>();
        deck.add(card);

        Player player = new Player(id, deck);
        player.drawCard();

        return player;
    }

    @Test
    void shouldApplyEchoBonus() {

        Card first = createCard(
                "p1-c1", 3, 8, Echo.DOMINIO);

        Card second = createCard(
                "p2-c1", 2, 8, Echo.SAPIENZA);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        resolver.resolve(player1, player2);

        // Dominio attacks Sapienza with +1 ATK.
        assertEquals(4, second.getCurrentDefense());

        // Sapienza receives no bonus against Dominio.
        assertEquals(6, first.getCurrentDefense());
    }

    @Test
    void shouldApplyReciprocalEchoBonus() {

        Card first = createCard(
                "p1-c1", 2, 8, Echo.DOMINIO);

        Card second = createCard(
                "p2-c1", 2, 8, Echo.OBLIO);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        resolver.resolve(player1, player2);

        assertEquals(5, first.getCurrentDefense());
        assertEquals(5, second.getCurrentDefense());
    }

    @Test
    void shouldApplyOverflowWhenAttackerSurvives() {

        Card first = createCard(
                "p1-c1", 6, 8, Echo.DOMINIO);

        Card second = createCard(
                "p2-c1", 1, 3, Echo.DOMINIO);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        resolver.resolve(player1, player2);

        assertFalse(first.isDestroyed());
        assertTrue(second.isDestroyed());

        // Attack 6 - defense 3 = 3 overflow.
        assertEquals(10, player1.getHealth());
        assertEquals(7, player2.getHealth());
    }

    @Test
    void shouldNotApplyOverflowWhenBothCardsAreDestroyed() {

        Card first = createCard(
                "p1-c1", 5, 3, Echo.DOMINIO);

        Card second = createCard(
                "p2-c1", 5, 3, Echo.DOMINIO);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        resolver.resolve(player1, player2);

        assertTrue(first.isDestroyed());
        assertTrue(second.isDestroyed());

        assertEquals(10, player1.getHealth());
        assertEquals(10, player2.getHealth());
    }

    @Test
    void shouldIncludePersistentAttackBonus() {

        Card first = createCard(
                "p1-c1", 3, 8, Echo.DOMINIO);

        Card second = createCard(
                "p2-c1", 1, 8, Echo.DOMINIO);

        first.addAttackBonus(2);

        Player player1 = createPlayer("p1", first);
        Player player2 = createPlayer("p2", second);

        resolver.resolve(player1, player2);

        // Printed attack 3 + persistent bonus 2.
        assertEquals(3, second.getCurrentDefense());
    }

    @Test
    void shouldRejectMissingActiveCard() {

        Player player1 = new Player(
                "p1",
                new ArrayList<>()
        );

        Card second = createCard(
                "p2-c1", 3, 4, Echo.DOMINIO);

        Player player2 = createPlayer("p2", second);

        assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(player1, player2)
        );
    }
}
