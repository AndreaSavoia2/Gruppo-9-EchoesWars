package com.echoeswars;

import java.util.ArrayList;

public class CardsDrawnEvent extends ReplayEvent {

    private final ArrayList<DrawnCard> cards;

    public CardsDrawnEvent(ArrayList<DrawnCard> cards) {

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
