package com.echoeswars;

public abstract class ReplayEvent {

    private final String phase;
    private final String type;

    protected ReplayEvent(String phase, String type) {
        this.phase = phase;
        this.type = type;
    }

    public String getPhase() {
        return phase;
    }

    public String getType() {
        return type;
    }
}
