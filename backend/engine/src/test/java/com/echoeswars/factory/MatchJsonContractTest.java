package com.echoeswars.factory;

import com.echoeswars.game.Game;
import com.echoeswars.replay.Replay;
import com.echoeswars.replay.ReplayRound;
import com.echoeswars.replay.ReplayStateReconstructor;
import com.echoeswars.result.MatchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchJsonContractTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GameFactory factory = new GameFactory();

    private JsonNode validInput() throws Exception {
        return mapper.readTree("""
            {
              "matchId":"contract-test", "seed":42, "rulesVersion":"1.3",
              "cardDefinitions":[
                {"cardId":"A","attack":3,"defense":5,"cost":1,"echo":"DOMINIO","school":"GUERRIERI","effect":"NONE"},
                {"cardId":"B","attack":2,"defense":6,"cost":1,"echo":"SAPIENZA","school":"MAGHI","effect":"NONE"}
              ],
              "players":[
                {"playerId":"p1","deck":[{"cardId":"A","quantity":3}]},
                {"playerId":"p2","deck":[{"cardId":"B","quantity":3}]}
              ]
            }
            """);
    }

    @Test
    void shouldRoundTripEntireMatchOutput() throws Exception {
        Game game = factory.create(validInput());
        game.play();
        MatchResult result = game.getMatchResult();
        JsonNode output = mapper.valueToTree(result);
        assertEquals("contract-test", output.get("matchId").asText());
        assertTrue(output.has("replay"));

        Replay replay = mapper.treeToValue(output.get("replay"), Replay.class);
        ReplayStateReconstructor reconstructor =
                new ReplayStateReconstructor(replay.getInitialState());
        for (ReplayRound round : replay.getRounds()) {
            reconstructor.applyRound(round);
        }
        assertEquals(mapper.valueToTree(replay.getFinalState()),
                mapper.valueToTree(reconstructor.snapshot()));
        assertEquals(output.get("replay"), mapper.valueToTree(replay));
    }

    @Test
    void shouldRejectMissingAndInvalidInputFields() throws Exception {
        JsonNode missingMatchId = validInput();
        ((com.fasterxml.jackson.databind.node.ObjectNode) missingMatchId).remove("matchId");
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> factory.create(missingMatchId)).getMessage().contains("matchId"));

        JsonNode invalidSeed = validInput();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalidSeed).put("seed", "wrong");
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> factory.create(invalidSeed)).getMessage().contains("seed"));

        JsonNode invalidQuantity = validInput();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalidQuantity.get("players").get(0)
                .get("deck").get(0)).put("quantity", -1);
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> factory.create(invalidQuantity)).getMessage().contains("quantity"));

        JsonNode invalidDefinitions = validInput();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalidDefinitions).remove("cardDefinitions");
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> factory.create(invalidDefinitions)).getMessage().contains("cardDefinitions"));
    }
}
