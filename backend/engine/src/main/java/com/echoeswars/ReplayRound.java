package com.echoeswars;

import java.util.ArrayList;

public class ReplayRound {

    private final int round;
    private final ArrayList<ReplayEvent> events;

    public ReplayRound(int round) {

        // Store the round number
        this.round = round;

        // Initialize the event list
        this.events = new ArrayList<>();
    }

    public int getRound() {
        return round;
    }

    public void addEvent(ReplayEvent event) {

        // Add an event to this round
        events.add(event);
    }

    public ArrayList<ReplayEvent> getEvents() {

        // Return a copy of the event list
        return new ArrayList<>(events);
    }
}
