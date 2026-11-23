package com.echoeswars.replay.events;

import java.util.ArrayList;

import com.echoeswars.replay.ReplayEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CardsPlayedEvent extends ReplayEvent {

    private final ArrayList<PlayedCard> cards;

    @JsonCreator
    public CardsPlayedEvent(
            @JsonProperty("cards") ArrayList<PlayedCard> cards) {

        super("PLAY", "CARDS_PLAYED");
        this.cards = new ArrayList<>(cards);
    }

    public ArrayList<PlayedCard> getCards() {
        return new ArrayList<>(cards);
    }
}
