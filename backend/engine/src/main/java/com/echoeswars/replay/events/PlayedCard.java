package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayedCard {

    private final String playerId;
    private final String cardId;
    private final String instanceId;

    @JsonCreator
    public PlayedCard(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("cardId") String cardId,
            @JsonProperty("instanceId") String instanceId) {

        this.playerId = playerId;
        this.cardId = cardId;
        this.instanceId = instanceId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getCardId() {
        return cardId;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
