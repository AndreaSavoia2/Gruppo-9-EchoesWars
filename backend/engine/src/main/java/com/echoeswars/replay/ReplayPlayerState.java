
package com.echoeswars.replay;

import com.echoeswars.model.Card;
import com.echoeswars.model.Player;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplayPlayerState {

    private final String playerId;
    private final int health;
    private final ReplayCardState activeCard;
    private final List<ReplayCardState> deck;
    private final List<ReplayCardState> discardPile;
    private final String lastDestroyedSchool;

    public ReplayPlayerState(Player player) {

        this.playerId = player.getPlayerId();
        this.health = player.getHealth();

        Card active = player.getActiveCard();

        this.activeCard = active == null
                ? null
                : new ReplayCardState(active);

        this.deck = new ArrayList<>();
        for (Card card : player.getDeck()) {
            deck.add(new ReplayCardState(card));
        }

        this.discardPile = new ArrayList<>();
        for (Card card : player.getDiscardPile()) {
            discardPile.add(new ReplayCardState(card));
        }

        this.lastDestroyedSchool = player.getLastDestroyedSchool() == null
                ? null
                : player.getLastDestroyedSchool().name();
    }

    @JsonCreator
    public ReplayPlayerState(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("health") int health,
            @JsonProperty("activeCard") ReplayCardState activeCard,
            @JsonProperty("deck") List<ReplayCardState> deck,
            @JsonProperty("discardPile") List<ReplayCardState> discardPile,
            @JsonProperty("lastDestroyedSchool") String lastDestroyedSchool) {

        this.playerId = playerId;
        this.health = health;
        this.activeCard = activeCard;
        this.deck = new ArrayList<>(deck);
        this.discardPile = new ArrayList<>(discardPile);
        this.lastDestroyedSchool = lastDestroyedSchool;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getHealth() {
        return health;
    }

    public ReplayCardState getActiveCard() {
        return activeCard;
    }

    public List<ReplayCardState> getDeck() {
        return new ArrayList<>(deck);
    }

    public List<ReplayCardState> getDiscardPile() {
        return new ArrayList<>(discardPile);
    }

    public String getLastDestroyedSchool() {
        return lastDestroyedSchool;
    }
}
