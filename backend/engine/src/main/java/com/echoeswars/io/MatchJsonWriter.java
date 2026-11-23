package com.echoeswars.io;

import com.echoeswars.result.MatchResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;

public class MatchJsonWriter {

    private final ObjectMapper mapper;

    public MatchJsonWriter() {

        this.mapper = new ObjectMapper();

        // Preserve the existing pretty-printed JSON format.
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** Writes the compact, versioned replay wire format. */
    public void writeCompact(Path outputPath, MatchResult result) throws IOException {
        if (outputPath == null || result == null) {
            throw new IllegalArgumentException("Output path and result cannot be null.");
        }
        mapper.writeValue(outputPath.toFile(),
                new CompactReplayCodec().encode(mapper.valueToTree(result)));
    }

    /**
     * Writes the completed match result to a JSON file.
     *
     * @param outputPath destination file path
     * @param result     completed match result
     * @throws IOException if the result cannot be written
     */
    public void write(
            Path outputPath,
            MatchResult result) throws IOException {

        if (outputPath == null) {
            throw new IllegalArgumentException(
                    "Output path cannot be null.");
        }

        if (result == null) {
            throw new IllegalArgumentException(
                    "Match result cannot be null.");
        }

        mapper.writeValue(
                outputPath.toFile(),
                result);
    }
}
