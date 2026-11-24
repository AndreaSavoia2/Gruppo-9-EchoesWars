
package com.echoeswars.input;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MatchJsonReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReadValidMatchJson() throws IOException {

        Path inputFile = tempDir.resolve("match.json");

        String json = """
                {
                  "matchId": "test-match",
                  "seed": 12345,
                  "rulesVersion": "1.3",
                  "players": [],
                  "cardDefinitions": []
                }
                """;

        Files.writeString(inputFile, json);

        MatchJsonReader reader = new MatchJsonReader();

        JsonNode root = reader.read(inputFile);

        assertEquals(
                "test-match",
                root.get("matchId").asText());

        assertEquals(
                12345L,
                root.get("seed").asLong());

        assertEquals(
                "1.3",
                root.get("rulesVersion").asText());

        assertTrue(root.get("players").isArray());
        assertTrue(root.get("cardDefinitions").isArray());
    }

    @Test
    void shouldRejectInvalidJson() throws IOException {

        Path inputFile = tempDir.resolve("invalid.json");

        Files.writeString(
                inputFile,
                "{ invalid json }");

        MatchJsonReader reader = new MatchJsonReader();

        assertThrows(
                IOException.class,
                () -> reader.read(inputFile));
    }

    @Test
    void shouldRejectMissingFile() {

        Path missingFile = tempDir.resolve("missing.json");

        MatchJsonReader reader = new MatchJsonReader();

        assertThrows(
                IOException.class,
                () -> reader.read(missingFile));
    }

    @Test
    void shouldRejectNullPath() {

        MatchJsonReader reader = new MatchJsonReader();

        assertThrows(
                IllegalArgumentException.class,
                () -> reader.read(null));
    }
}
