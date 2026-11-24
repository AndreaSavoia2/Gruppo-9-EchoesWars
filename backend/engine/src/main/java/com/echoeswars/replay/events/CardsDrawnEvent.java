package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

import com.echoeswars.replay.ReplayEvent;

public class CardsDrawnEvent extends ReplayEvent {

    private final ArrayList<DrawnCard> cards;

    @JsonCreator
    public CardsDrawnEvent(@JsonProperty("cards") ArrayList<DrawnCard> cards) {

        // Initialize the common event information
        super("DRAW", "CARDS_DRAWN");

        // Store a copy of the drawn cards
        this.cards = new ArrayList<>(cards);
    }

    public ArrayList<DrawnCard> getCards() {

        // Return a copy of the card list
        return new ArrayList<>(cards);
    }
}
