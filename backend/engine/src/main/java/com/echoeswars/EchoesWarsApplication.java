package com.echoeswars;

import com.echoeswars.factory.GameFactory;
import com.echoeswars.game.Game;
import com.echoeswars.input.MatchJsonReader;
import com.echoeswars.io.MatchJsonWriter;
import com.echoeswars.result.MatchResult;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

public class EchoesWarsApplication {

    private final MatchJsonReader reader;
    private final GameFactory gameFactory;
    private final MatchJsonWriter writer;

    public EchoesWarsApplication() {

        this.reader = new MatchJsonReader();
        this.gameFactory = new GameFactory();
        this.writer = new MatchJsonWriter();
    }

    /**
     * Runs the application and returns an exit code.
     *
     * @param args command-line arguments
     * @return 0 on success, 1 on failure
     */
    public int run(String[] args) {

        try {
            // Accept zero, one or two paths, relative to the current directory.
            if (args == null || args.length > 2) {
                throw new IllegalArgumentException(
                        "Usage: java -jar engine.jar [input.json] [output.json]");
            }

            // Support launching from either backend/engine or the repository root.
            Path defaultInput = Path.of("data/input/match.json");
            Path defaultOutput = Path.of("data/output/match-result.json");
            if (!Files.isRegularFile(defaultInput)) {
                defaultInput = Path.of("backend/engine/data/input/match.json");
                defaultOutput = Path.of("backend/engine/data/output/match-result.json");
            }
            Path inputPath = args.length > 0 ? Path.of(args[0]) : defaultInput;
            Path outputPath = args.length > 1 ? Path.of(args[1]) : defaultOutput;

            // Read the match configuration.
            JsonNode root = reader.read(inputPath);

            // Create and execute the match.
            Game game = gameFactory.create(root);

            game.play();

            // Obtain the completed match result.
            MatchResult result = game.getMatchResult();

            // Create the destination directory when necessary.
            Path outputDirectory = outputPath.toAbsolutePath().getParent();
            if (outputDirectory != null) {
                Files.createDirectories(outputDirectory);
            }
            writer.writeCompact(outputPath, result);

            System.out.println(
                    "Match result saved to: "
                            + outputPath.toString());

            return 0;

        } catch (IOException e) {

            System.err.println(
                    "File error: " + e.getMessage());

            return 1;

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            System.err.println(
                    "Match error: " + e.getMessage());

            return 1;

        } catch (RuntimeException e) {

            System.err.println(
                    "Unexpected engine error: " + e.getMessage());

            return 1;
        }
    }
}
