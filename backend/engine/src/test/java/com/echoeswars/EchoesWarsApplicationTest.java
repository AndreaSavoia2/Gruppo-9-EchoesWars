
package com.echoeswars;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EchoesWarsApplicationTest {

        @TempDir
        Path tempDir;

        private final ObjectMapper mapper = new ObjectMapper();

        private Path createValidInput() throws IOException {

                Path inputFile = tempDir.resolve("match.json");

                String json = """
                                {
                                  "matchId": "application-test",
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

                Files.writeString(inputFile, json);

                return inputFile;
        }

        @Test
        void shouldRunMatchAndGenerateJson() throws IOException {

                Path inputFile = createValidInput();
                Path outputFile = tempDir.resolve("result.json");

                EchoesWarsApplication application = new EchoesWarsApplication();

                int exitCode = application.run(
                                new String[] {
                                                inputFile.toString(),
                                                outputFile.toString()
                                });

                assertEquals(0, exitCode);
                assertTrue(Files.exists(outputFile));

                JsonNode result = mapper.readTree(
                                outputFile.toFile());

                assertNotNull(result);
                assertTrue(result.isObject());

                assertEquals(
                                "application-test",
                                result.get("matchId").asText());
        }

        @Test
        void shouldReturnErrorForMissingInputFile() {

                Path inputFile = tempDir.resolve("missing.json");
                Path outputFile = tempDir.resolve("result.json");

                EchoesWarsApplication application = new EchoesWarsApplication();

                int exitCode = application.run(
                                new String[] {
                                                inputFile.toString(),
                                                outputFile.toString()
                                });

                assertEquals(1, exitCode);
                assertFalse(Files.exists(outputFile));
        }

        @Test
        void shouldReturnErrorForInvalidJson() throws IOException {

                Path inputFile = tempDir.resolve("invalid.json");
                Path outputFile = tempDir.resolve("result.json");

                Files.writeString(
                                inputFile,
                                "{ invalid json }");

                EchoesWarsApplication application = new EchoesWarsApplication();

                int exitCode = application.run(
                                new String[] {
                                                inputFile.toString(),
                                                outputFile.toString()
                                });

                assertEquals(1, exitCode);
                assertFalse(Files.exists(outputFile));
        }

        @Test
        void shouldReturnErrorForUnknownCard() throws IOException {

                Path inputFile = tempDir.resolve("unknown-card.json");
                Path outputFile = tempDir.resolve("result.json");

                String json = """
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
                                """;

                Files.writeString(inputFile, json);

                EchoesWarsApplication application = new EchoesWarsApplication();

                int exitCode = application.run(
                                new String[] {
                                                inputFile.toString(),
                                                outputFile.toString()
                                });

                assertEquals(1, exitCode);
                assertFalse(Files.exists(outputFile));
        }

        @Test
        void shouldCreateMissingOutputDirectoryAndWriteResult()
                        throws IOException {

                Path inputFile = createValidInput();

                Path outputFile = tempDir
                                .resolve("missing-directory")
                                .resolve("result.json");

                EchoesWarsApplication application = new EchoesWarsApplication();

                int exitCode = application.run(
                                new String[] {
                                                inputFile.toString(),
                                                outputFile.toString()
                                });

                assertEquals(0, exitCode);
                assertTrue(Files.isDirectory(outputFile.getParent()));
                assertTrue(Files.isRegularFile(outputFile));

                JsonNode result = mapper.readTree(outputFile.toFile());

                assertEquals(
                                "application-test",
                                result.path("matchId").asText());

                assertEquals(
                                2,
                                result.path("replay").path("formatVersion").asInt());
        }
}
