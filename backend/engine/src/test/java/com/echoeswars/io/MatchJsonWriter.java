package com.echoeswars.io;

import com.echoeswars.factory.GameFactory;
import com.echoeswars.game.Game;
import com.echoeswars.result.MatchResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MatchJsonWriterTest {

    @TempDir
    Path tempDir;

    private final MatchJsonWriter writer = new MatchJsonWriter();
    private final ObjectMapper mapper = new ObjectMapper();

    private MatchResult createMatchResult() throws Exception {

        String json = """
                {
                  "matchId": "writer-test",
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
                      "school": "MAGHI",
                      "effect": "NONE"
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

        JsonNode root = mapper.readTree(json);

        Game game = new GameFactory().create(root);

        game.play();

        return game.getMatchResult();
    }

    @Test
    void shouldWriteValidMatchResultJson() throws Exception {

        MatchResult result = createMatchResult();

        Path outputFile = tempDir.resolve("result.json");

        writer.write(outputFile, result);

        assertTrue(Files.exists(outputFile));
        assertTrue(Files.size(outputFile) > 0);

        JsonNode writtenJson = mapper.readTree(
                outputFile.toFile());

        assertNotNull(writtenJson);
        assertTrue(writtenJson.isObject());
    }

    @Test
    void shouldPreserveMatchResultData() throws Exception {

        MatchResult result = createMatchResult();

        Path outputFile = tempDir.resolve("result.json");

        writer.write(outputFile, result);

        JsonNode expected = mapper.valueToTree(result);

        JsonNode actual = mapper.readTree(
                outputFile.toFile());

        assertEquals(expected, actual);
    }

    @Test
    void shouldWriteIndentedJson() throws Exception {

        MatchResult result = createMatchResult();

        Path outputFile = tempDir.resolve("result.json");

        writer.write(outputFile, result);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("\n"));
        assertTrue(content.contains("  \""));
    }

    @Test
    void shouldRejectNullOutputPath() throws Exception {

        MatchResult result = createMatchResult();

        assertThrows(
                IllegalArgumentException.class,
                () -> writer.write(null, result));
    }

    @Test
    void shouldRejectNullMatchResult() {

        Path outputFile = tempDir.resolve("result.json");

        assertThrows(
                IllegalArgumentException.class,
                () -> writer.write(outputFile, null));
    }

    @Test
    void shouldReportMissingParentDirectory() throws Exception {

        MatchResult result = createMatchResult();

        Path outputFile = tempDir
                .resolve("missing-directory")
                .resolve("result.json");

        assertThrows(
                IOException.class,
                () -> writer.write(outputFile, result));
    }
    @Test
    void shouldCompactAndRestoreReplayWithoutLosingState() throws Exception {
        MatchResult result = createMatchResult();
        Path outputFile = tempDir.resolve("compact-result.json");
        writer.writeCompact(outputFile, result);

        JsonNode compact = mapper.readTree(outputFile.toFile());
        JsonNode replayNode = compact.path("replay");
        assertEquals(2, replayNode.path("formatVersion").asInt());
        assertTrue(replayNode.path("cardDefinitions").has("C001"));
        assertFalse(replayNode.has("finalState"));
        JsonNode firstCard = replayNode.path("initialState")
                .path("players").get(0).path("deck").get(0);
        assertTrue(firstCard.has("cardId"));
        assertFalse(firstCard.has("attack"));

        com.echoeswars.replay.Replay restored =
                new CompactReplayCodec().decode(replayNode);
        assertEquals(mapper.valueToTree(result.getReplay().getInitialState()),
                mapper.valueToTree(restored.getInitialState()));
        assertEquals(mapper.valueToTree(result.getReplay().getFinalState()),
                mapper.valueToTree(restored.getFinalState()));
        assertEquals(mapper.valueToTree(result.getReplay().getRounds()),
                mapper.valueToTree(restored.getRounds()));

        Path legacyFile = tempDir.resolve("legacy-result.json");
        writer.write(legacyFile, result);
        assertTrue(Files.size(outputFile) < Files.size(legacyFile));
    }

}
