package com.echoeswars;

public class CombatCardResult {

    private final String instanceId;
    private final String cardId;
    private final int attack;
    private final int defenseBefore;
    private final int defenseAfter;
    private final boolean destroyed;

    public CombatCardResult(
            String instanceId,
            String cardId,
            int attack,
            int defenseBefore,
            int defenseAfter,
            boolean destroyed) {
        this.instanceId = instanceId;
        this.cardId = cardId;
        this.attack = attack;
        this.defenseBefore = defenseBefore;
        this.defenseAfter = defenseAfter;
        this.destroyed = destroyed;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getCardId() {
        return cardId;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefenseBefore() {
        return defenseBefore;
    }

    public int getDefenseAfter() {
        return defenseAfter;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
