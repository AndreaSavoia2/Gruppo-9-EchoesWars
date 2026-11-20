package com.echoeswars.replay;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplayRound {

    private final int round;
    private final ArrayList<ReplayEvent> events;

    public ReplayRound(int round) {
        this.round = round;
        this.events = new ArrayList<>();
    }

    @JsonCreator
    public ReplayRound(
            @JsonProperty("round") int round,
            @JsonProperty("events") ArrayList<ReplayEvent> events) {

        if (events == null) {
            throw new IllegalArgumentException(
                    "Replay round events cannot be null.");
        }

        this.round = round;
        this.events = new ArrayList<>(events);
    }

    public int getRound() {
        return round;
    }

    public void addEvent(ReplayEvent event) {
        events.add(event);
    }

    public ArrayList<ReplayEvent> getEvents() {
        return new ArrayList<>(events);
    }
}
