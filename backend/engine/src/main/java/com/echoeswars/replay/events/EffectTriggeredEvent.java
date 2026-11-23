
package com.echoeswars.replay.events;

import com.echoeswars.replay.ReplayEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class EffectTriggeredEvent extends ReplayEvent {

    private final String effect;
    private final String sourceInstanceId;
    private final String sourceCardId;
    private final Map<String, Integer> changes;
    private final ArrayList<DiscardedCard> discardedCards;
    private final String analysisTargetIdAfter;

    // Backward-compatible constructor.
    public EffectTriggeredEvent(
            String phase,
            String effect,
            String sourceInstanceId,
            String sourceCardId,
            Map<String, Integer> changes) {

        this(
                phase,
                effect,
                sourceInstanceId,
                sourceCardId,
                changes,
                new ArrayList<>(),
                null);
    }

    // Constructor supporting discarded card identities.
    public EffectTriggeredEvent(
            String phase,
            String effect,
            String sourceInstanceId,
            String sourceCardId,
            Map<String, Integer> changes,
            ArrayList<DiscardedCard> discardedCards) {

        this(
                phase,
                effect,
                sourceInstanceId,
                sourceCardId,
                changes,
                discardedCards,
                null);
    }

    // Constructor supporting discarded cards and Analysis.
    @JsonCreator
    public EffectTriggeredEvent(
            @JsonProperty("phase") String phase,
            @JsonProperty("effect") String effect,
            @JsonProperty("sourceInstanceId") String sourceInstanceId,
            @JsonProperty("sourceCardId") String sourceCardId,
            @JsonProperty("changes") Map<String, Integer> changes,
            @JsonProperty("discardedCards") ArrayList<DiscardedCard> discardedCards,
            @JsonProperty("analysisTargetIdAfter") String analysisTargetIdAfter) {

        super(phase, "EFFECT_TRIGGERED");

        this.effect = effect;
        this.sourceInstanceId = sourceInstanceId;
        this.sourceCardId = sourceCardId;

        this.changes = new LinkedHashMap<>(changes);
        this.discardedCards = discardedCards == null ? new ArrayList<>() : new ArrayList<>(discardedCards);
        this.analysisTargetIdAfter = analysisTargetIdAfter;
    }

    public String getEffect() {
        return effect;
    }

    public String getSourceInstanceId() {
        return sourceInstanceId;
    }

    public String getSourceCardId() {
        return sourceCardId;
    }

    public Map<String, Integer> getChanges() {
        return new LinkedHashMap<>(changes);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public ArrayList<DiscardedCard> getDiscardedCards() {
        return new ArrayList<>(discardedCards);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getAnalysisTargetIdAfter() {
        return analysisTargetIdAfter;
    }
}
