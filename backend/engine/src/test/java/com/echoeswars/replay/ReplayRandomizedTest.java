
package com.echoeswars.replay;

import com.echoeswars.factory.GameFactory;
import com.echoeswars.game.Game;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.replay.events.EffectTriggeredEvent;
import com.echoeswars.combat.CombatCardResult;
import com.echoeswars.replay.events.CombatResolvedEvent;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReplayRandomizedTest {

        private static final int MATCH_COUNT = 100;
        private static final long TEST_SEED = 123456789L;

        private final ObjectMapper mapper = new ObjectMapper();

        @Test
        void shouldReconstructRandomizedMatches() throws Exception {

                Random random = new Random(TEST_SEED);

                Set<String> triggeredEffects = new HashSet<>();
                Set<String> combatEffectsCovered = new HashSet<>();

                int verifiedRounds = 0;

                for (int matchIndex = 0; matchIndex < MATCH_COUNT; matchIndex++) {

                        long matchSeed = random.nextLong();

                        JsonNode configuration = createMatchConfiguration(
                                        matchIndex,
                                        matchSeed,
                                        random);

                        Game game = new GameFactory().create(configuration);
                        Map<Integer, JsonNode> expectedStates = new HashMap<>();

                        game.setRoundStateObserver((roundNumber, state) -> {
                                expectedStates.put(
                                                roundNumber,
                                                mapper.valueToTree(state));
                        });
                        game.play();

                        Replay replay = game.getMatchResult().getReplay();

                        // Exercise the real JSON transport boundary, not just in-memory events.
                        String serializedReplay = mapper.writeValueAsString(replay);
                        Replay restoredReplay = mapper.readValue(serializedReplay, Replay.class);
                        assertEquals(mapper.readTree(serializedReplay), mapper.valueToTree(restoredReplay),
                                        "JSON round-trip mismatch: match=" + matchIndex);

                        JsonNode replayJson = mapper.valueToTree(restoredReplay);

                        for (JsonNode roundJson : replayJson.path("rounds")) {
                                assertFalse(
                                                roundJson.has("stateAfter"),
                                                "Replay JSON must not contain stateAfter");
                        }

                        ReplayStateReconstructor reconstructor = new ReplayStateReconstructor(
                                        restoredReplay.getInitialState());

                        for (ReplayRound round : restoredReplay.getRounds()) {

                                for (ReplayEvent event : round.getEvents()) {

                                        if (event instanceof EffectTriggeredEvent) {

                                                EffectTriggeredEvent effectEvent = (EffectTriggeredEvent) event;

                                                triggeredEffects.add(effectEvent.getEffect());
                                        }

                                        if (event instanceof CombatResolvedEvent) {

                                                CombatResolvedEvent combatEvent = (CombatResolvedEvent) event;

                                                for (CombatCardResult card : combatEvent.getCards()) {

                                                        Map<String, Integer> attack = card.getAttackBreakdown();

                                                        Map<String, Integer> damage = card.getDamageBreakdown();

                                                        // Duelist: bonus against a Mage card.
                                                        if (attack.getOrDefault("duelistBonus", 0) > 0) {
                                                                combatEffectsCovered.add("DUELLANTE");
                                                        }

                                                        // Opportunist: bonus against an already damaged card.
                                                        if (attack.getOrDefault("opportunistBonus", 0) > 0) {
                                                                combatEffectsCovered.add("OPPORTUNISTA");
                                                        }

                                                        // Resistance: actual reduction of incoming damage.
                                                        if (damage.getOrDefault(
                                                                        "resistanceReduction", 0) > 0) {

                                                                combatEffectsCovered.add("RESISTENZA");
                                                        }

                                                        // Piercing: bypass of active Resistance.
                                                        if (Boolean.TRUE.equals(
                                                                        card.getPiercingBypassedResistance())) {

                                                                combatEffectsCovered.add("PERFORANTE");
                                                        }

                                                        // Reflection: actual reflected damage.
                                                        if (card.getReflectionDamage() != null
                                                                        && card.getReflectionDamage() > 0) {

                                                                combatEffectsCovered.add("RIFLESSO");
                                                        }
                                                }
                                        }
                                }

                                ReplayGameState reconstructed = reconstructor.applyRound(round);

                                assertNotNull(
                                                expectedStates.get(round.getRound()),
                                                "Missing observed state at round " + round.getRound());

                                assertEquals(
                                                expectedStates.get(round.getRound()),
                                                mapper.valueToTree(reconstructed),
                                                "Replay mismatch: match="
                                                                + matchIndex
                                                                + ", seed=" + matchSeed
                                                                + ", round=" + round.getRound());

                                verifiedRounds++;
                        }

                        assertEquals(
                                        mapper.valueToTree(restoredReplay.getFinalState()),
                                        mapper.valueToTree(reconstructor.snapshot()),
                                        "Final state mismatch: match="
                                                        + matchIndex
                                                        + ", seed=" + matchSeed);
                }

                System.out.println(
                                "Verified matches: " + MATCH_COUNT);

                System.out.println(
                                "Verified rounds: " + verifiedRounds);

                System.out.println(
                                "Triggered effects: " + triggeredEffects);

                System.out.println(
                                "Combat effects covered: " + combatEffectsCovered);

                Set<String> allCoveredEffects = new HashSet<>(triggeredEffects);

                allCoveredEffects.addAll(combatEffectsCovered);

                // Build the expected set of all card effects.
                Set<String> expectedEffects = new HashSet<>();

                for (CardEffect effect : CardEffect.values()) {
                        if (effect != CardEffect.NONE) {
                                expectedEffects.add(effect.name());
                        }
                }

                // Verify that every effect was exercised.
                assertEquals(
                                expectedEffects,
                                allCoveredEffects,
                                "Not all card effects were exercised");
                allCoveredEffects.addAll(combatEffectsCovered);

                System.out.println(
                                "Total covered effects: "
                                                + allCoveredEffects.size()
                                                + "/20");

                System.out.println(
                                "All covered effects: " + allCoveredEffects);

                assertTrue(verifiedRounds > 0);
        }

        private ObjectNode createMatchConfiguration(
                        int matchIndex,
                        long matchSeed,
                        Random random) {

                ObjectNode root = mapper.createObjectNode();

                root.put("matchId", "random-match-" + matchIndex);
                root.put("seed", matchSeed);
                root.put("rulesVersion", "rules-1.0");

                ArrayNode definitions = root.putArray("cardDefinitions");

                CardEffect[] effects = CardEffect.values();
                Echo[] echoes = Echo.values();
                CardSchool[] schools = CardSchool.values();

                // Generate one definition for every available effect.
                for (CardEffect effect : effects) {

                        ObjectNode card = definitions.addObject();

                        card.put("cardId", "CARD-" + effect.name());
                        card.put("attack", random.nextInt(5) + 1);
                        card.put("defense", random.nextInt(8) + 3);
                        card.put("cost", 1);
                        card.put("echo",
                                        echoes[random.nextInt(echoes.length)].name());
                        card.put("school",
                                        schools[random.nextInt(schools.length)].name());
                        card.put("effect", effect.name());
                }

                ArrayNode players = root.putArray("players");

                for (int playerIndex = 0; playerIndex < 2; playerIndex++) {

                        ObjectNode player = players.addObject();

                        player.put("playerId", "player-" + (playerIndex + 1));

                        ArrayNode deck = player.putArray("deck");

                        // Give each player 12 cards.
                        for (int i = 0; i < 12; i++) {

                                CardEffect effect = effects[random.nextInt(effects.length)];

                                ObjectNode entry = deck.addObject();
                                entry.put("cardId", "CARD-" + effect.name());
                                entry.put("quantity", 1);
                        }
                }

                return root;
        }
}
