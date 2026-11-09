package com.echoeswars;

public class DrawnCard {

    private final String playerId;
    private final String cardId;
    private final String instanceId;

    public DrawnCard(
            String playerId,
            String cardId,
            String instanceId
    ) {
        // Store the information about a drawn card
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
