package com.echoeswars.replay;

import com.echoeswars.replay.events.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = CardDestroyedEvent.class,
                name = "CARD_DESTROYED"),

        @JsonSubTypes.Type(
                value = CardReplacedEvent.class,
                name = "CARD_REPLACED"),

        @JsonSubTypes.Type(
                value = CardRoundAdvancedEvent.class,
                name = "CARD_ROUND_ADVANCED"),

        @JsonSubTypes.Type(
                value = CardsDrawnEvent.class,
                name = "CARDS_DRAWN"),

        @JsonSubTypes.Type(
                value = CardsPlayedEvent.class,
                name = "CARDS_PLAYED"),

        @JsonSubTypes.Type(
                value = CombatResolvedEvent.class,
                name = "COMBAT_RESOLVED"),

        @JsonSubTypes.Type(
                value = EffectTriggeredEvent.class,
                name = "EFFECT_TRIGGERED"),

        @JsonSubTypes.Type(
                value = RoundEndedEvent.class,
                name = "ROUND_ENDED")
})

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
