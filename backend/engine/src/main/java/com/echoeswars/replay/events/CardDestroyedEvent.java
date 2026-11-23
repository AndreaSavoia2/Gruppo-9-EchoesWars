package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.echoeswars.replay.ReplayEvent;

public class CardDestroyedEvent extends ReplayEvent {

    private final String playerId;
    private final String instanceId;
    private final String cardId;

    @JsonCreator
    public CardDestroyedEvent(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("instanceId") String instanceId,
            @JsonProperty("cardId") String cardId) {

        super("DESTRUCTION", "CARD_DESTROYED");

        this.playerId = playerId;
        this.instanceId = instanceId;
        this.cardId = cardId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getCardId() {
        return cardId;
    }
}
