
package com.echoeswars.replay;

import com.echoeswars.model.Card;
import com.echoeswars.model.Player;
import com.echoeswars.replay.events.CardsPlayedEvent;
import com.echoeswars.replay.events.PlayedCard;
import com.echoeswars.replay.events.CardsPlayedEvent;
import com.echoeswars.replay.events.PlayedCard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ReplayJsonRoundTripTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerializeAndDeserializeReplay() throws Exception {

        ArrayList<Card> firstDeck = new ArrayList<>();
        firstDeck.add(new Card("p1-c1", "CARD-1", 2, 10, 1));

        ArrayList<Card> secondDeck = new ArrayList<>();
        secondDeck.add(new Card("p2-c1", "CARD-2", 2, 10, 1));

        Player player1 = new Player("player-1", firstDeck);
        Player player2 = new Player("player-2", secondDeck);

        player1.drawCard();
        player2.drawCard();

        Replay replay = new Replay(new ArrayList<>());

        replay.setInitialState(
                new ReplayGameState(player1, player2));

        replay.setFinalState(
                new ReplayGameState(player1, player2));

        ReplayRound round = new ReplayRound(1);

        ArrayList<PlayedCard> playedCards = new ArrayList<>();

        playedCards.add(
                new PlayedCard("player-1", "CARD-1", "p1-c1"));

        playedCards.add(
                new PlayedCard("player-2", "CARD-2", "p2-c1"));

        round.addEvent(new CardsPlayedEvent(playedCards));

        replay.addRound(round);

        String json = mapper.writeValueAsString(replay);

        JsonNode originalJson = mapper.readTree(json);

        Replay restored = mapper.readValue(json, Replay.class);
        assertEquals(1, restored.getRounds().size());

        ReplayEvent restoredEvent = restored.getRounds().get(0).getEvents().get(0);

        assertInstanceOf(CardsPlayedEvent.class, restoredEvent);

        CardsPlayedEvent restoredCardsPlayed = (CardsPlayedEvent) restoredEvent;

        assertEquals(2, restoredCardsPlayed.getCards().size());

        assertEquals(
                "p1-c1",
                restoredCardsPlayed.getCards().get(0).getInstanceId());

        assertEquals(
                "p2-c1",
                restoredCardsPlayed.getCards().get(1).getInstanceId());

        assertEquals(
                originalJson,
                mapper.valueToTree(restored));
    }
}
