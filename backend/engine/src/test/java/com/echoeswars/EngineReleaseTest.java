package com.echoeswars;

import com.echoeswars.factory.GameFactory;
import com.echoeswars.game.Game;
import com.echoeswars.io.CompactReplayCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EngineReleaseTest {
    @TempDir Path temporaryDirectory;
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode input() throws Exception {
        return mapper.readTree("""
                {
                  "matchId":"release-check", "rulesVersion":"1.0", "seed":123456,
                  "cardDefinitions":[
                    {"cardId":"A","attack":4,"defense":5,"cost":1,"echo":"DOMINIO","school":"GUERRIERI","effect":"FURIA"},
                    {"cardId":"B","attack":3,"defense":6,"cost":1,"echo":"SAPIENZA","school":"MAGHI","effect":"RIFLESSO"}
                  ],
                  "players":[
                    {"playerId":"p1","deck":[{"cardId":"A","quantity":6}]},
                    {"playerId":"p2","deck":[{"cardId":"B","quantity":6}]}
                  ]
                }
                """);
    }

    @Test
    void sameSeedAndConfigurationProduceIdenticalCompactOutput() throws Exception {
        JsonNode expected = null;
        for (int i = 0; i < 3; i++) {
            Game game = new GameFactory().create(input());
            game.play();
            JsonNode actual = new CompactReplayCodec().encode(
                    mapper.valueToTree(game.getMatchResult()));
            if (expected == null) {
                expected = actual;
            } else {
                assertEquals(expected, actual, "A fixed seed must reproduce the full match");
            }
            assertEquals(2, actual.path("replay").path("formatVersion").asInt());
        }
    }

    @Test
    void applicationWritesCompactJsonToExplicitPaths() throws Exception {
        Path inputPath = temporaryDirectory.resolve("input.json");
        Path outputPath = temporaryDirectory.resolve("nested/output/result.json");
        mapper.writeValue(inputPath.toFile(), input());
        assertEquals(0, new EchoesWarsApplication().run(
                new String[]{inputPath.toString(), outputPath.toString()}));
        assertTrue(Files.isRegularFile(outputPath));
        JsonNode result = mapper.readTree(outputPath.toFile());
        assertEquals("release-check", result.path("matchId").asText());
        assertEquals(2, result.path("replay").path("formatVersion").asInt());
        assertFalse(result.path("replay").has("finalState"));
    }

    @Test
    void applicationRejectsMalformedInputAndExcessArguments() throws Exception {
        Path invalidInput = temporaryDirectory.resolve("invalid.json");
        Path output = temporaryDirectory.resolve("result.json");
        Files.writeString(invalidInput, "{not valid JSON");
        EchoesWarsApplication app = new EchoesWarsApplication();
        assertEquals(1, app.run(new String[]{invalidInput.toString(), output.toString()}));
        assertFalse(Files.exists(output));
        assertEquals(1, app.run(new String[]{"one", "two", "three"}));
    }
}
