package com.echoeswars.replay;

import com.echoeswars.model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplayCardState {

    private final String instanceId;
    private final String cardId;

    private final int attack;
    private final int defense;
    private final int cost;

    private final String echo;
    private final String school;
    private final String effect;

    private final int currentDefense;
    private final int maxDefense;
    private final int protection;
    private final int attackBonus;
    private final int temporaryAttackBonus;
    private final int roundsInPlay;
    private final int healingDone;

    private final String analysisTargetId;

    public ReplayCardState(Card card) {

        this.instanceId = card.getInstanceId();
        this.cardId = card.getCardId();

        this.attack = card.getAttack();
        this.defense = card.getDefense();
        this.cost = card.getCost();

        this.echo = card.getEcho().name();
        this.school = card.getSchool().name();
        this.effect = card.getEffect().name();

        this.currentDefense = card.getCurrentDefense();
        this.maxDefense = card.getMaxDefense();
        this.protection = card.getProtection();
        this.attackBonus = card.getAttackBonus();
        this.temporaryAttackBonus = card.getTemporaryAttackBonus();

        this.roundsInPlay = card.getRoundsInPlay();
        this.healingDone = card.getHealingDone();
        this.analysisTargetId = card.getAnalysisTargetId();
    }

    @JsonCreator
    public ReplayCardState(
            @JsonProperty("instanceId") String instanceId,
            @JsonProperty("cardId") String cardId,
            @JsonProperty("attack") int attack,
            @JsonProperty("defense") int defense,
            @JsonProperty("cost") int cost,
            @JsonProperty("echo") String echo,
            @JsonProperty("school") String school,
            @JsonProperty("effect") String effect,
            @JsonProperty("currentDefense") int currentDefense,
            @JsonProperty("maxDefense") int maxDefense,
            @JsonProperty("protection") int protection,
            @JsonProperty("attackBonus") int attackBonus,
            @JsonProperty("temporaryAttackBonus") int temporaryAttackBonus,
            @JsonProperty("roundsInPlay") int roundsInPlay,
            @JsonProperty("healingDone") int healingDone,
            @JsonProperty("analysisTargetId") String analysisTargetId) {

        this.instanceId = instanceId;
        this.cardId = cardId;
        this.attack = attack;
        this.defense = defense;
        this.cost = cost;
        this.echo = echo;
        this.school = school;
        this.effect = effect;
        this.currentDefense = currentDefense;
        this.maxDefense = maxDefense;
        this.protection = protection;
        this.attackBonus = attackBonus;
        this.temporaryAttackBonus = temporaryAttackBonus;
        this.roundsInPlay = roundsInPlay;
        this.healingDone = healingDone;
        this.analysisTargetId = analysisTargetId;
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

    public int getDefense() {
        return defense;
    }

    public int getCost() {
        return cost;
    }

    public String getEcho() {
        return echo;
    }

    public String getSchool() {
        return school;
    }

    public String getEffect() {
        return effect;
    }

    public int getCurrentDefense() {
        return currentDefense;
    }

    public int getMaxDefense() {
        return maxDefense;
    }

    public int getProtection() {
        return protection;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getTemporaryAttackBonus() {
        return temporaryAttackBonus;
    }

    public int getRoundsInPlay() {
        return roundsInPlay;
    }

    public int getHealingDone() {
        return healingDone;
    }

    public String getAnalysisTargetId() {
        return analysisTargetId;
    }
}
