
package com.echoeswars.model;

public class Card {

    private final String instanceId;
    private final String cardId;

    // Printed card statistics
    private final int attack;
    private final int defense;
    private final int cost;

    // Card classification
    private final Echo echo;
    private final CardSchool school;
    private final CardEffect effect;

    // Mutable combat state
    private int currentDefense;
    private int maxDefense;
    private int protection;
    private int attackBonus;
    private int temporaryAttackBonus;

    // Effect state
    private int roundsInPlay;
    private int healingDone;
    private String analysisTargetId;

    public Card(
            String instanceId,
            String cardId,
            int attack,
            int defense,
            int cost,
            Echo echo,
            CardSchool school,
            CardEffect effect) {

        this.instanceId = instanceId;
        this.cardId = cardId;

        this.attack = attack;
        this.defense = defense;
        this.cost = cost;

        this.echo = echo;
        this.school = school;
        this.effect = effect;

        this.currentDefense = defense;
        this.maxDefense = defense;
        this.protection = 0;
        this.attackBonus = 0;
        this.temporaryAttackBonus = 0;

        this.roundsInPlay = 0;
        this.healingDone = 0;
        this.analysisTargetId = null;
    }

    // Legacy constructor for backward compatibility
    public Card(
            String instanceId,
            String cardId,
            int attack,
            int defense,
            int cost) {

        this(
                instanceId,
                cardId,
                attack,
                defense,
                cost,
                Echo.DOMINIO,
                CardSchool.GUERRIERI,
                CardEffect.NONE);
    }

    // Card identity

    public String getInstanceId() {
        return instanceId;
    }

    public String getCardId() {
        return cardId;
    }

    // Printed statistics

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getCost() {
        return cost;
    }

    // Card classification

    public Echo getEcho() {
        return echo;
    }

    public CardSchool getSchool() {
        return school;
    }

    public CardEffect getEffect() {
        return effect;
    }

    // Combat statistics

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

    public int getBaseEffectiveAttack() {
        return attack + attackBonus + temporaryAttackBonus;
    }

    // Effect state

    public int getRoundsInPlay() {
        return roundsInPlay;
    }

    public int getHealingDone() {
        return healingDone;
    }

    public String getAnalysisTargetId() {
        return analysisTargetId;
    }

    // Round management

    public void incrementRoundsInPlay() {
        roundsInPlay++;
    }

    // Attack modifiers

    public void addAttackBonus(int bonus) {
        if (bonus < 0) {
            throw new IllegalArgumentException(
                    "Attack bonus cannot be negative.");
        }

        attackBonus += bonus;
    }

    public int getTemporaryAttackBonus() {
        return temporaryAttackBonus;
    }

    public void addTemporaryAttackBonus(int bonus) {

        if (bonus < 0) {
            throw new IllegalArgumentException(
                    "Temporary attack bonus cannot be negative.");
        }

        temporaryAttackBonus += bonus;
    }

    public void clearTemporaryAttackBonus() {
        temporaryAttackBonus = 0;
    }

    // Defense modifiers

    public void increaseMaxDefense(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Defense bonus cannot be negative.");
        }

        maxDefense += amount;
        currentDefense += amount;
    }

    public void reduceCurrentDefense(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Defense reduction cannot be negative.");
        }

        currentDefense = Math.max(
                1,
                currentDefense - amount);
    }

    // Protection management

    public void addProtection(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Protection cannot be negative.");
        }

        protection += amount;
    }

    public void clearProtection() {
        protection = 0;
    }

    // Damage management

    public int takeDamage(int damage) {

        if (damage < 0) {
            throw new IllegalArgumentException(
                    "Damage cannot be negative.");
        }

        // Protection absorbs damage first
        int absorbed = Math.min(protection, damage);
        protection -= absorbed;

        int remainingDamage = damage - absorbed;

        // Defense can become negative to track overflow
        currentDefense -= remainingDamage;

        return remainingDamage;
    }

    public boolean isDestroyed() {
        return currentDefense <= 0;
    }

    // Healing effect tracking

    public void addHealingDone(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Healing amount cannot be negative.");
        }

        healingDone += amount;
    }

    // Analysis effect tracking

    public void setAnalysisTargetId(String targetId) {
        analysisTargetId = targetId;
    }
}
