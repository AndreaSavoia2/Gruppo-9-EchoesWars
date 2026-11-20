
package com.echoeswars.replay.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.echoeswars.replay.ReplayEvent;

public class CardRoundAdvancedEvent extends ReplayEvent {

    private final String playerId;
    private final String instanceId;
    private final int roundsInPlay;

    @JsonCreator
    public CardRoundAdvancedEvent(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("instanceId") String instanceId,
            @JsonProperty("roundsInPlay") int roundsInPlay) {

        super("ROUND_START", "CARD_ROUND_ADVANCED");

        this.playerId = playerId;
        this.instanceId = instanceId;
        this.roundsInPlay = roundsInPlay;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getRoundsInPlay() {
        return roundsInPlay;
    }
}
