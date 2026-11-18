package com.echoeswars.replay.events;

import java.util.ArrayList;

import com.echoeswars.replay.ReplayEvent;

public class CardsPlayedEvent extends ReplayEvent {

    private final ArrayList<PlayedCard> cards;

    public CardsPlayedEvent(ArrayList<PlayedCard> cards) {
        super("PLAY", "CARDS_PLAYED");
        this.cards = new ArrayList<>(cards);
    }

    public ArrayList<PlayedCard> getCards() {
        return new ArrayList<>(cards);
    }
}
