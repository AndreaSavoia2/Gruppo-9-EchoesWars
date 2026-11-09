package com.echoeswars;

import java.util.ArrayList;

public class Game {

    private Player player1;
    private Player player2;

    private int round;

    private static final int MAX_ROUNDS = 100;

    private final String matchId;
    private final String rulesVersionId;

    private final Replay replay;

    private static final String ENGINE_VERSION = "1.0.0";

    private String winnerId;
    private MatchStatus status;

    public Game(
            String matchId,
            String rulesVersionId,
            Player player1,
            Player player2) {

        // Store match metadata
        this.matchId = matchId;
        this.rulesVersionId = rulesVersionId;

        // Store the participating players
        this.player1 = player1;
        this.player2 = player2;

        // Capture player information before the match starts
        ArrayList<ReplayPlayer> replayPlayers = new ArrayList<>();

        replayPlayers.add(
                new ReplayPlayer(
                        player1.getPlayerId(),
                        player1.getHealth()));

        replayPlayers.add(
                new ReplayPlayer(
                        player2.getPlayerId(),
                        player2.getHealth()));

        // Initialize the match replay
        this.replay = new Replay(replayPlayers);

        // Initialize the match state
        this.round = 1;
        this.winnerId = null;
        this.status = MatchStatus.IN_PROGRESS;
    }

    private void addPlayedCard(
            ArrayList<PlayedCard> playedCards,
            Player player) {
        Card activeCard = player.getActiveCard();

        if (activeCard == null || activeCard.isDestroyed()) {
            return;
        }

        PlayedCard playedCard = new PlayedCard(
                player.getPlayerId(),
                activeCard.getCardId(),
                activeCard.getInstanceId());

        playedCards.add(playedCard);
    }

    public void play() {

        // Store cards drawn for the next round
        ArrayList<DrawnCard> pendingDraws = new ArrayList<>();

        // Continue until a player is defeated or the round limit is reached
        while (player1.canContinue()
                && player2.canContinue()
                && round <= MAX_ROUNDS) {

            // Create the replay record for this round
            ReplayRound replayRound = new ReplayRound(round);

            // Collect cards drawn for this round
            ArrayList<DrawnCard> drawnCards = new ArrayList<>();

            if (round == 1) {

                // Initial cards were drawn before Game.play()
                addDrawnCard(drawnCards, player1);
                addDrawnCard(drawnCards, player2);

            } else {

                // These cards were drawn after the previous combat
                drawnCards.addAll(pendingDraws);
                pendingDraws.clear();
            }

            // Record actual draws only
            if (!drawnCards.isEmpty()) {

                CardsDrawnEvent drawEvent = new CardsDrawnEvent(
                        drawnCards);

                replayRound.addEvent(drawEvent);
            }

            // Record the cards participating in this round
            ArrayList<PlayedCard> playedCards = new ArrayList<>();

            addPlayedCard(playedCards, player1);
            addPlayedCard(playedCards, player2);

            if (!playedCards.isEmpty()) {

                CardsPlayedEvent playEvent = new CardsPlayedEvent(
                        playedCards);

                replayRound.addEvent(playEvent);
            }

            // Resolve simultaneous combat and capture its result
            CombatResult combatResult = resolveCombat(
                    player1,
                    player2);

            // Create the combat event
            ReplayEvent combatEvent = new CombatResolvedEvent(
                    combatResult);

            // Store the combat event in the current round
            replayRound.addEvent(combatEvent);

            // Replace destroyed cards and remember actual draws
            if (player1.getActiveCard() != null
                    && player1.getActiveCard().isDestroyed()) {

                player1.replaceDestroyedCard();

                // Record the replacement only if one was drawn
                addDrawnCard(pendingDraws, player1);
            }

            if (player2.getActiveCard() != null
                    && player2.getActiveCard().isDestroyed()) {

                player2.replaceDestroyedCard();

                // Record the replacement only if one was drawn
                addDrawnCard(pendingDraws, player2);
            }

            // Capture player health at the end of the round
            PlayerLife playersLife = new PlayerLife(
                    player1.getHealth(),
                    player2.getHealth());

            // Create the round-ended event
            ReplayEvent roundEndedEvent = new RoundEndedEvent(
                    playersLife);

            // Add the event to the current round
            replayRound.addEvent(roundEndedEvent);

            // Store the completed round
            replay.addRound(replayRound);

            round++;
        }

        // Display the final match result
        printResult();
    }

    private void addDrawnCard(
            ArrayList<DrawnCard> drawnCards,
            Player player) {

        // Read the player's active card
        Card activeCard = player.getActiveCard();

        // Ignore players without an active card
        if (activeCard == null) {
            return;
        }

        // Capture the card's identity for the replay
        DrawnCard drawnCard = new DrawnCard(
                player.getPlayerId(),
                activeCard.getCardId(),
                activeCard.getInstanceId());

        drawnCards.add(drawnCard);
    }

    private void printResult() {

        // Determine and store the match outcome
        if (!player1.canContinue() && !player2.canContinue()) {

            status = MatchStatus.DRAW;
            winnerId = null;

        } else if (!player1.canContinue()) {

            status = MatchStatus.COMPLETED;
            winnerId = player2.getPlayerId();

        } else if (!player2.canContinue()) {

            status = MatchStatus.COMPLETED;
            winnerId = player1.getPlayerId();

        } else {

            status = MatchStatus.ROUND_LIMIT;
            winnerId = null;
        }
    }

    private CombatResult resolveCombat(Player firstPlayer, Player secondPlayer) {

        // Get the active cards
        Card firstCard = firstPlayer.getActiveCard();
        Card secondCard = secondPlayer.getActiveCard();

        // Track damage received by each player
        int firstPlayerDamage = 0;
        int secondPlayerDamage = 0;

        // Both players must have an active card
        if (firstCard == null || secondCard == null) {
            System.out.println("Combat cannot start: missing active card.");
            return null;
        }

        // Read both attack values before applying damage
        int firstAttack = firstCard.getAttack();
        int secondAttack = secondCard.getAttack();

        // Capture defense values before combat
        int firstDefenseBefore = firstCard.getCurrentDefense();
        int secondDefenseBefore = secondCard.getCurrentDefense();

        // Apply simultaneous damage
        firstCard.takeDamage(secondAttack);
        secondCard.takeDamage(firstAttack);

        // Capture defense values after combat
        int firstDefenseAfter = firstCard.getCurrentDefense();
        int secondDefenseAfter = secondCard.getCurrentDefense();

        // Create the first card's combat snapshot
        CombatCardResult firstCardResult = new CombatCardResult(
                firstCard.getInstanceId(),
                firstCard.getCardId(),
                firstAttack,
                firstDefenseBefore,
                Math.max(0, firstDefenseAfter),
                firstCard.isDestroyed());

        // Create the second card's combat snapshot
        CombatCardResult secondCardResult = new CombatCardResult(
                secondCard.getInstanceId(),
                secondCard.getCardId(),
                secondAttack,
                secondDefenseBefore,
                Math.max(0, secondDefenseAfter),
                secondCard.isDestroyed());

        // Apply overflow damage only if the attacking card survives
        if (!firstCard.isDestroyed() && secondCard.isDestroyed()) {

            int overflowDamage = Math.abs(
                    secondCard.getCurrentDefense());

            secondPlayerDamage = overflowDamage;
            secondPlayer.takeDamage(overflowDamage);
        }

        if (!secondCard.isDestroyed() && firstCard.isDestroyed()) {

            int overflowDamage = Math.abs(
                    firstCard.getCurrentDefense());

            firstPlayerDamage = overflowDamage;
            firstPlayer.takeDamage(overflowDamage);
        }

        // Create the complete combat result
        CombatResult combatResult = new CombatResult(
                firstCardResult,
                secondCardResult,
                firstPlayerDamage,
                secondPlayerDamage);

        // Return the completed combat result
        return combatResult;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public int getRoundsPlayed() {
        return round - 1;
    }

    public MatchResult getMatchResult() {

        // Prevent retrieving a result before the match finishes
        if (status == MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "The match has not finished yet.");
        }

        // Capture the final health of both players
        PlayerLife currentLife = new PlayerLife(
                player1.getHealth(),
                player2.getHealth());

        // Create a snapshot of the final match information
        return new MatchResult(
                matchId,
                rulesVersionId,
                ENGINE_VERSION,
                status,
                winnerId,
                round - 1,
                currentLife,
                replay);
    }

}
