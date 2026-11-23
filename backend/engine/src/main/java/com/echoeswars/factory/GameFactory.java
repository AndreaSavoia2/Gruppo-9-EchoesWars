package com.echoeswars.factory;

import com.echoeswars.game.Game;

import com.echoeswars.model.Card;
import com.echoeswars.model.CardEffect;
import com.echoeswars.model.CardSchool;
import com.echoeswars.model.Echo;
import com.echoeswars.model.Player;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameFactory {

    private static void validate(JsonNode root) {
        if (!root.isObject()) {
            throw new IllegalArgumentException("Match configuration must be a JSON object.");
        }
        requiredText(root, "matchId", "matchId");
        requiredText(root, "rulesVersion", "rulesVersion");
        JsonNode seed = root.get("seed");
        if (seed == null || !seed.isIntegralNumber() || !seed.canConvertToLong()) {
            throw new IllegalArgumentException("seed must be a 64-bit integer.");
        }
        JsonNode definitions = requiredArray(root, "cardDefinitions", "cardDefinitions");
        for (int i = 0; i < definitions.size(); i++) {
            JsonNode card = definitions.get(i);
            String path = "cardDefinitions[" + i + "]";
            if (!card.isObject()) {
                throw new IllegalArgumentException(path + " must be an object.");
            }
            requiredText(card, "cardId", path + ".cardId");
            requiredNonNegativeInt(card, "attack", path + ".attack");
            requiredNonNegativeInt(card, "defense", path + ".defense");
            requiredNonNegativeInt(card, "cost", path + ".cost");
            requiredText(card, "echo", path + ".echo");
            requiredText(card, "school", path + ".school");
            if (card.has("effect")) {
                requiredText(card, "effect", path + ".effect");
            }
        }
        JsonNode players = requiredArray(root, "players", "players");
        if (players.size() != 2) {
            throw new IllegalArgumentException("A match requires exactly two players.");
        }
        for (int i = 0; i < players.size(); i++) {
            JsonNode player = players.get(i);
            String path = "players[" + i + "]";
            if (!player.isObject()) {
                throw new IllegalArgumentException(path + " must be an object.");
            }
            requiredText(player, "playerId", path + ".playerId");
            JsonNode deck = requiredArray(player, "deck", path + ".deck");
            for (int j = 0; j < deck.size(); j++) {
                JsonNode entry = deck.get(j);
                String entryPath = path + ".deck[" + j + "]";
                if (!entry.isObject()) {
                    throw new IllegalArgumentException(entryPath + " must be an object.");
                }
                requiredText(entry, "cardId", entryPath + ".cardId");
                requiredNonNegativeInt(entry, "quantity", entryPath + ".quantity");
            }
        }
    }

    private static String requiredText(JsonNode node, String key, String path) {
        JsonNode value = node.get(key);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException(path + " must be a non-empty string.");
        }
        return value.asText();
    }

    private static JsonNode requiredArray(JsonNode node, String key, String path) {
        JsonNode value = node.get(key);
        if (value == null || !value.isArray()) {
            throw new IllegalArgumentException(path + " must be an array.");
        }
        return value;
    }

    private static void requiredNonNegativeInt(JsonNode node, String key, String path) {
        JsonNode value = node.get(key);
        if (value == null || !value.isInt() || value.intValue() < 0) {
            throw new IllegalArgumentException(path + " must be a non-negative integer.");
        }
    }

    /**
     * Creates a complete match from its JSON configuration.
     *
     * Preserves card instance IDs, deck shuffling order
     * and initial draws.
     */
    public Game create(JsonNode root) {

        if (root == null) {
            throw new IllegalArgumentException(
                    "Match configuration cannot be null.");
        }

        validate(root);

        String matchId = root.get("matchId").asText();
        long seed = root.get("seed").asLong();
        String rulesVersionId = root.get("rulesVersion").asText();

        // Create card definitions.
        HashMap<String, Card> cardMap = new HashMap<>();

        for (JsonNode cardNode : root.get("cardDefinitions")) {

            String cardId = cardNode.get("cardId").asText();
            int attack = cardNode.get("attack").asInt();
            int defense = cardNode.get("defense").asInt();
            int cost = cardNode.get("cost").asInt();

            Echo echo = Echo.valueOf(
                    cardNode.path("echo").asText());

            CardSchool school = CardSchool.valueOf(
                    cardNode.path("school").asText());

            CardEffect effect = CardEffect.valueOf(
                    cardNode.path("effect").asText("NONE"));

            Card card = new Card(
                    "definition-" + cardId,
                    cardId,
                    attack,
                    defense,
                    cost,
                    echo,
                    school,
                    effect);

            cardMap.put(cardId, card);
        }

        // Create players and their decks.
        ArrayList<Player> gamePlayers = new ArrayList<>();

        int playerNumber = 1;

        for (JsonNode playerNode : root.get("players")) {

            int cardNumber = 1;

            String playerId = playerNode.get("playerId").asText();

            ArrayList<Card> playerDeck = new ArrayList<>();

            for (JsonNode deckEntry : playerNode.get("deck")) {

                String cardId = deckEntry.get("cardId").asText();
                int quantity = deckEntry.get("quantity").asInt();

                Card cardDefinition = cardMap.get(cardId);

                if (cardDefinition == null) {
                    throw new IllegalArgumentException(
                            "Unknown card: " + cardId);
                }

                // Create independent card instances.
                for (int i = 0; i < quantity; i++) {

                    String instanceId =
                            "p" + playerNumber + "-c" + cardNumber;

                    cardNumber++;

                    Card cardCopy = new Card(
                            instanceId,
                            cardDefinition.getCardId(),
                            cardDefinition.getAttack(),
                            cardDefinition.getDefense(),
                            cardDefinition.getCost(),
                            cardDefinition.getEcho(),
                            cardDefinition.getSchool(),
                            cardDefinition.getEffect());

                    playerDeck.add(cardCopy);
                }
            }

            Player player = new Player(
                    playerId,
                    playerDeck);

            gamePlayers.add(player);

            playerNumber++;
        }

        // Use one shared Random for all players.
        Random random = new Random(seed);

        for (Player player : gamePlayers) {
            player.shuffleDeck(random);
        }

        // Draw initial active cards.
        for (Player player : gamePlayers) {
            player.drawCard();
        }

        // Preserve the existing player-count validation.
        if (gamePlayers.size() != 2) {
            throw new IllegalArgumentException(
                    "A match requires exactly two players.");
        }

        Player player1 = gamePlayers.get(0);
        Player player2 = gamePlayers.get(1);

        return new Game(
                matchId,
                rulesVersionId,
                player1,
                player2);
    }
}
