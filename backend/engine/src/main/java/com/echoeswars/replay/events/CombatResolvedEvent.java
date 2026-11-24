package com.echoeswars.replay.events;

import com.echoeswars.combat.CombatCardResult;
import com.echoeswars.combat.CombatResult;
import com.echoeswars.combat.PlayerLife;
import com.echoeswars.replay.ReplayEvent;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonPropertyOrder({
        "phase",
        "type",
        "cards",
        "playerDamage"
})
public class CombatResolvedEvent extends ReplayEvent {

    private final CombatResult combatResult;

    public CombatResolvedEvent(CombatResult combatResult) {

        // Initialize the common event information
        super("COMBAT", "COMBAT_RESOLVED");

        // Store the complete combat result
        this.combatResult = combatResult;
    }

    @JsonCreator
    public CombatResolvedEvent(
            @JsonProperty("cards") ArrayList<CombatCardResult> cards,
            @JsonProperty("playerDamage") PlayerLife playerDamage) {
        super("COMBAT", "COMBAT_RESOLVED");
        if (cards == null || cards.size() != 2 || playerDamage == null) {
            throw new IllegalArgumentException("Combat event requires two cards and player damage");
        }
        // These flags are not part of the replay JSON and are not used by the reconstructor.
        this.combatResult = new CombatResult(
                cards.get(0), cards.get(1),
                playerDamage.getPlayerOne(), playerDamage.getPlayerTwo());
    }

    public ArrayList<CombatCardResult> getCards() {
        return combatResult.getCards();
    }

    public PlayerLife getPlayerDamage() {
        return combatResult.getPlayerDamage();
    }
}
