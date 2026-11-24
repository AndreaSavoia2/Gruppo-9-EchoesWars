package com.echoeswars.replay;

import com.echoeswars.model.Player;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplayGameState {

    private final List<ReplayPlayerState> players;

    public ReplayGameState(Player player1, Player player2) {

        this.players = List.of(
                new ReplayPlayerState(player1),
                new ReplayPlayerState(player2));
    }

    public List<ReplayPlayerState> getPlayers() {
        return players;
    }

    @JsonCreator
    public ReplayGameState(
            @JsonProperty("players") List<ReplayPlayerState> players) {

        if (players == null || players.size() != 2) {
            throw new IllegalArgumentException(
                    "A replay game state must contain exactly two players");
        }

        this.players = List.copyOf(players);
    }
}
