
package com.echoeswars.factory;

import com.echoeswars.game.Game;
import com.echoeswars.game.MatchStatus;
import com.echoeswars.result.MatchResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameFactoryTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final GameFactory factory = new GameFactory();

    private JsonNode createMatchJson() throws Exception {

        String json = """
                {
                  "matchId": "factory-test",
                  "rulesVersion": "1.3",
                  "seed": 12345,
                  "cardDefinitions": [
                    {
                      "cardId": "C001",
                      "attack": 3,
                      "defense": 5,
                      "cost": 2,
                      "echo": "DOMINIO",
                      "school": "GUERRIERI",
                      "effect": "NONE"
                    },
                    {
                      "cardId": "C002",
                      "attack": 2,
                      "defense": 6,
                      "cost": 2,
                      "echo": "SAPIENZA",
                      "school": "MAGHI"
                    }
                  ],
                  "players": [
                    {
                      "playerId": "player-1",
                      "deck": [
                        {
                          "cardId": "C001",
                          "quantity": 3
                        }
                      ]
                    },
                    {
                      "playerId": "player-2",
                      "deck": [
                        {
                          "cardId": "C002",
                          "quantity": 3
                        }
                      ]
                    }
                  ]
                }
                """;

        return mapper.readTree(json);
    }

    @Test
    void shouldCreateAndPlayMatch() throws Exception {

        JsonNode root = createMatchJson();

        Game game = factory.create(root);

        assertNotNull(game);
        assertEquals(
                MatchStatus.IN_PROGRESS,
                game.getStatus());

        game.play();

        MatchResult result = game.getMatchResult();

        assertNotNull(result);
        assertNotEquals(
                MatchStatus.IN_PROGRESS,
                game.getStatus());

        assertTrue(game.getRoundsPlayed() > 0);
    }

    @Test
    void shouldProduceDeterministicResultWithSameSeed()
            throws Exception {

        JsonNode root = createMatchJson();

        Game firstGame = factory.create(root);
        Game secondGame = factory.create(root);

        firstGame.play();
        secondGame.play();

        assertEquals(
                firstGame.getStatus(),
                secondGame.getStatus());

        assertEquals(
                firstGame.getWinnerId(),
                secondGame.getWinnerId());

        assertEquals(
                firstGame.getRoundsPlayed(),
                secondGame.getRoundsPlayed());

        // Compare the complete serialized results.
        JsonNode firstResult = mapper.valueToTree(
                firstGame.getMatchResult());

        JsonNode secondResult = mapper.valueToTree(
                secondGame.getMatchResult());

        assertEquals(firstResult, secondResult);
    }

    @Test
    void shouldRejectUnknownCard() throws Exception {

        JsonNode root = mapper.readTree("""
                {
                  "matchId": "invalid-match",
                  "rulesVersion": "1.3",
                  "seed": 1,
                  "cardDefinitions": [],
                  "players": [
                    {
                      "playerId": "player-1",
                      "deck": [
                        {
                          "cardId": "UNKNOWN",
                          "quantity": 1
                        }
                      ]
                    },
                    {
                      "playerId": "player-2",
                      "deck": []
                    }
                  ]
                }
                """);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.create(root));

        assertEquals(
                "Unknown card: UNKNOWN",
                exception.getMessage());
    }

    @Test
    void shouldRejectInvalidPlayerCount() throws Exception {

        JsonNode root = mapper.readTree("""
                {
                  "matchId": "invalid-players",
                  "rulesVersion": "1.3",
                  "seed": 1,
                  "cardDefinitions": [],
                  "players": [
                    {
                      "playerId": "only-player",
                      "deck": []
                    }
                  ]
                }
                """);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.create(root));

        assertEquals(
                "A match requires exactly two players.",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullConfiguration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> factory.create(null));
    }
}
