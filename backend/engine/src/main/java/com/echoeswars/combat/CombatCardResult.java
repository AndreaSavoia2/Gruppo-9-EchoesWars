
package com.echoeswars.combat;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CombatCardResult {

    private final String instanceId;
    private final String cardId;
    private final int attack;
    private final int defenseBefore;
    private final int defenseAfter;
    private final boolean destroyed;
    private final Integer protectionBefore;
    private final Integer protectionAfter;

    private final Map<String, Integer> attackBreakdown;
    private final Map<String, Integer> damageBreakdown;
    private final Integer reflectionDamage;
    private final Boolean piercingBypassedResistance;

    public CombatCardResult(
            String instanceId,
            String cardId,
            int attack,
            int defenseBefore,
            int defenseAfter,
            boolean destroyed) {

        this(
                instanceId,
                cardId,
                attack,
                defenseBefore,
                defenseAfter,
                destroyed,
                null,
                null,
                null,
                null);
    }

    public CombatCardResult(
            String instanceId,
            String cardId,
            int attack,
            int defenseBefore,
            int defenseAfter,
            boolean destroyed,
            Map<String, Integer> attackBreakdown,
            Map<String, Integer> damageBreakdown,
            Integer reflectionDamage,
            Boolean piercingBypassedResistance) {

        this(
                instanceId,
                cardId,
                attack,
                defenseBefore,
                defenseAfter,
                destroyed,
                attackBreakdown,
                damageBreakdown,
                reflectionDamage,
                piercingBypassedResistance,
                null,
                null);
    }

    @JsonCreator
    public CombatCardResult(
            @JsonProperty("instanceId") String instanceId,
            @JsonProperty("cardId") String cardId,
            @JsonProperty("attack") int attack,
            @JsonProperty("defenseBefore") int defenseBefore,
            @JsonProperty("defenseAfter") int defenseAfter,
            @JsonProperty("destroyed") boolean destroyed,
            @JsonProperty("attackBreakdown") Map<String, Integer> attackBreakdown,
            @JsonProperty("damageBreakdown") Map<String, Integer> damageBreakdown,
            @JsonProperty("reflectionDamage") Integer reflectionDamage,
            @JsonProperty("piercingBypassedResistance") Boolean piercingBypassedResistance,
            @JsonProperty("protectionBefore") Integer protectionBefore,
            @JsonProperty("protectionAfter") Integer protectionAfter) {

        this.instanceId = instanceId;
        this.cardId = cardId;
        this.attack = attack;
        this.defenseBefore = defenseBefore;
        this.defenseAfter = defenseAfter;
        this.destroyed = destroyed;

        this.attackBreakdown = attackBreakdown == null
                ? null
                : new LinkedHashMap<>(attackBreakdown);

        this.damageBreakdown = damageBreakdown == null
                ? null
                : new LinkedHashMap<>(damageBreakdown);

        this.reflectionDamage = reflectionDamage;
        this.piercingBypassedResistance = piercingBypassedResistance;

        this.protectionBefore = protectionBefore;
        this.protectionAfter = protectionAfter;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Integer> getAttackBreakdown() {
        return attackBreakdown == null
                ? null
                : new LinkedHashMap<>(attackBreakdown);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Integer> getDamageBreakdown() {
        return damageBreakdown == null
                ? null
                : new LinkedHashMap<>(damageBreakdown);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getReflectionDamage() {
        return reflectionDamage;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getPiercingBypassedResistance() {
        return piercingBypassedResistance;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getProtectionBefore() {
        return protectionBefore;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getProtectionAfter() {
        return protectionAfter;
    }

}
