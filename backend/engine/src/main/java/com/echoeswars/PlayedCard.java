package com.echoeswars;

public class PlayedCard {

    private final String playerId;
    private final String cardId;
    private final String instanceId;

    public PlayedCard(
            String playerId,
            String cardId,
            String instanceId) {
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
