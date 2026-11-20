package com.echoeswars.input;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class MatchJsonReader {

    private final ObjectMapper mapper;

    public MatchJsonReader() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Reads a match configuration from a JSON file.
     *
     * @param inputPath path to the JSON configuration
     * @return parsed JSON root node
     * @throws IOException if the file cannot be read or parsed
     */
    public JsonNode read(Path inputPath) throws IOException {

        if (inputPath == null) {
            throw new IllegalArgumentException(
                    "Input path cannot be null.");
        }

        return mapper.readTree(inputPath.toFile());
    }
}
