package com.echoeswars.io;

import com.echoeswars.replay.Replay;
import com.echoeswars.replay.ReplayRound;
import com.echoeswars.replay.ReplayStateReconstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/** Compact wire format for lossless replay state reconstruction. */
public final class CompactReplayCodec {
    private static final String[] STATIC_FIELDS = {
            "attack", "defense", "cost", "echo", "school", "effect"
    };
    private static final String[] CARD_COLLECTIONS = {"deck", "discardPile"};
    private final ObjectMapper mapper = new ObjectMapper();

    public ObjectNode encode(JsonNode fullResult) {
        ObjectNode output = fullResult.deepCopy();
        ObjectNode replay = (ObjectNode) output.path("replay");
        if (!replay.hasNonNull("initialState")) {
            throw new IllegalArgumentException("Replay initialState is required");
        }
        ObjectNode definitions = mapper.createObjectNode();
        compactState((ObjectNode) replay.get("initialState"), definitions);
        replay.set("cardDefinitions", definitions);
        replay.remove("finalState");
        replay.put("formatVersion", 2);
        return output;
    }

    /** Expands a compact replay into the original Java Replay model. */
    public Replay decode(JsonNode compactReplay) throws IOException {
        if (compactReplay.path("formatVersion").asInt(-1) != 2) {
            throw new IllegalArgumentException("Unsupported compact replay formatVersion");
        }
        ObjectNode expanded = compactReplay.deepCopy();
        JsonNode definitions = expanded.path("cardDefinitions");
        if (!definitions.isObject()) {
            throw new IllegalArgumentException("Missing cardDefinitions");
        }
        expandState((ObjectNode) expanded.path("initialState"), definitions);
        expanded.remove("cardDefinitions");
        expanded.remove("formatVersion");
        expanded.remove("finalState");
        Replay replay = mapper.treeToValue(expanded, Replay.class);
        ReplayStateReconstructor reconstructor =
                new ReplayStateReconstructor(replay.getInitialState());
        for (ReplayRound round : replay.getRounds()) {
            reconstructor.applyRound(round);
        }
        replay.setFinalState(reconstructor.snapshot());
        return replay;
    }

    private void compactState(ObjectNode state, ObjectNode definitions) {
        for (JsonNode player : state.path("players")) {
            ObjectNode p = (ObjectNode) player;
            if (p.path("activeCard").isObject()) {
                compactCard((ObjectNode) p.get("activeCard"), definitions);
            }
            for (String collection : CARD_COLLECTIONS) {
                for (JsonNode card : p.path(collection)) {
                    compactCard((ObjectNode) card, definitions);
                }
            }
        }
    }

    private void compactCard(ObjectNode card, ObjectNode definitions) {
        String cardId = card.path("cardId").asText();
        ObjectNode definition = mapper.createObjectNode();
        for (String field : STATIC_FIELDS) {
            definition.set(field, card.get(field));
            card.remove(field);
        }
        if (definitions.has(cardId) && !definitions.get(cardId).equals(definition)) {
            throw new IllegalArgumentException("Conflicting card definition: " + cardId);
        }
        definitions.set(cardId, definition);
        // Store only mutable values that differ from their initial defaults.
        removeIfEqual(card, "currentDefense", definition.path("defense").asInt());
        removeIfEqual(card, "maxDefense", definition.path("defense").asInt());
        for (String field : new String[] {"protection", "attackBonus", "temporaryAttackBonus",
                "roundsInPlay", "healingDone"}) {
            removeIfEqual(card, field, 0);
        }
        if (card.path("analysisTargetId").isNull()) {
            card.remove("analysisTargetId");
        }
    }

    private void removeIfEqual(ObjectNode card, String field, int expected) {
        if (card.path(field).isNumber() && card.path(field).asInt() == expected) {
            card.remove(field);
        }
    }

    private void expandState(ObjectNode state, JsonNode definitions) {
        for (JsonNode player : state.path("players")) {
            ObjectNode p = (ObjectNode) player;
            if (p.path("activeCard").isObject()) {
                expandCard((ObjectNode) p.get("activeCard"), definitions);
            }
            for (String collection : CARD_COLLECTIONS) {
                for (JsonNode card : p.path(collection)) {
                    expandCard((ObjectNode) card, definitions);
                }
            }
        }
    }

    private void expandCard(ObjectNode card, JsonNode definitions) {
        String id = card.path("cardId").asText();
        JsonNode definition = definitions.path(id);
        if (!definition.isObject()) {
            throw new IllegalArgumentException("Unknown card definition: " + id);
        }
        for (String field : STATIC_FIELDS) {
            if (!definition.has(field)) {
                throw new IllegalArgumentException("Missing card definition field: " + field);
            }
            card.set(field, definition.get(field));
        }
        putDefault(card, "currentDefense", definition.path("defense").asInt());
        putDefault(card, "maxDefense", definition.path("defense").asInt());
        for (String field : new String[] {"protection", "attackBonus", "temporaryAttackBonus",
                "roundsInPlay", "healingDone"}) {
            putDefault(card, field, 0);
        }
        if (!card.has("analysisTargetId")) {
            card.putNull("analysisTargetId");
        }
    }

    private void putDefault(ObjectNode card, String field, int value) {
        if (!card.has(field)) {
            card.put(field, value);
        }
    }
}
