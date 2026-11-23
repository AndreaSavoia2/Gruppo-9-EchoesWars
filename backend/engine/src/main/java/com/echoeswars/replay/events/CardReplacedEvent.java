package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.echoeswars.replay.ReplayEvent;

public class CardReplacedEvent extends ReplayEvent {

    private final String playerId;
    private final String destroyedInstanceId;
    private final String newInstanceId;
    private final String newCardId;

    @JsonCreator
    public CardReplacedEvent(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("destroyedInstanceId") String destroyedInstanceId,
            @JsonProperty("newInstanceId") String newInstanceId,
            @JsonProperty("newCardId") String newCardId) {

        super("REPLACEMENT", "CARD_REPLACED");

        this.playerId = playerId;
        this.destroyedInstanceId = destroyedInstanceId;
        this.newInstanceId = newInstanceId;
        this.newCardId = newCardId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDestroyedInstanceId() {
        return destroyedInstanceId;
    }

    public String getNewInstanceId() {
        return newInstanceId;
    }

    public String getNewCardId() {
        return newCardId;
    }
}
