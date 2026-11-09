package com.echoeswars;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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

    public ArrayList<CombatCardResult> getCards() {
        return combatResult.getCards();
    }

    public PlayerLife getPlayerDamage() {
        return combatResult.getPlayerDamage();
    }
}
