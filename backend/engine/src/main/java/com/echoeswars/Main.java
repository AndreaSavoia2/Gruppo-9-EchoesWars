package com.echoeswars;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        try {
            // Load match configuration
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(
                    Path.of("backend/engine/data/input/match.json").toFile());

            String matchId = root.get("matchId").asText();
            long seed = root.get("seed").asLong();
            String rulesVersionId = root.get("rulesVersion").asText();

            // Create card definitions
            HashMap<String, Card> cardMap = new HashMap<>();

            for (JsonNode cardNode : root.get("cardDefinitions")) {

                String cardId = cardNode.get("cardId").asText();
                int attack = cardNode.get("attack").asInt();
                int defense = cardNode.get("defense").asInt();
                int cost = cardNode.get("cost").asInt();

                // Create a card definition, not a playable instance
                Card card = new Card(
                        "definition-" + cardId,
                        cardId,
                        attack,
                        defense,
                        cost);

                cardMap.put(cardId, card);
            }

            // Create players and their decks
            ArrayList<Player> gamePlayers = new ArrayList<>();

            // Track the player's position
            int playerNumber = 1;

            for (JsonNode playerNode : root.get("players")) {

                // Start card numbering from 1 for each player
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

                    // Create independent card instances
                    for (int i = 0; i < quantity; i++) {

                        // Generate an identifier based on player and card number
                        String instanceId = "p" + playerNumber + "-c" + cardNumber;
                        cardNumber++;

                        Card cardCopy = new Card(
                                instanceId,
                                cardDefinition.getCardId(),
                                cardDefinition.getAttack(),
                                cardDefinition.getDefense(),
                                cardDefinition.getCost());

                        playerDeck.add(cardCopy);

                    }
                }

                Player player = new Player(playerId, playerDeck);
                gamePlayers.add(player);

                // Move to the next player
                playerNumber++;
            }

            // Shuffle decks using the match seed
            Random random = new Random(seed);

            for (Player player : gamePlayers) {
                Collections.shuffle(player.getDeck(), random);
            }

            // Draw the initial active card for each player
            for (Player player : gamePlayers) {
                player.drawCard();
            }

            // Resolve the first combat
            if (gamePlayers.size() != 2) {
                throw new IllegalArgumentException(
                        "A match requires exactly two players.");
            }

            Player player1 = gamePlayers.get(0);
            Player player2 = gamePlayers.get(1);

            Game game = new Game(
                    matchId,
                    rulesVersionId,
                    player1,
                    player2);

            game.play();

            MatchResult result = game.getMatchResult();

            // Configure JSON formatting
            ObjectMapper outputMapper = new ObjectMapper();
            outputMapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Save the match result to a JSON file
            File outputFile = new File("backend/engine/data/output/match-result.json");

            outputMapper.writeValue(outputFile, result);

            System.out.println("Match result saved to: " + outputFile.getPath());

        } catch (IOException e) {
            System.out.println(
                    "Error reading match: " + e.getMessage());
        }
    }
}
