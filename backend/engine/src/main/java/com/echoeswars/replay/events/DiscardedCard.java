package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DiscardedCard {

    private final String instanceId;
    private final String cardId;

    @JsonCreator
    public DiscardedCard(
            @JsonProperty("instanceId") String instanceId,
            @JsonProperty("cardId") String cardId) {

        this.instanceId = instanceId;
        this.cardId = cardId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getCardId() {
        return cardId;
    }
}
