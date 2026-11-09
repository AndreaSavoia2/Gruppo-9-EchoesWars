package com.echoeswars;

public class Card {

    private String cardId;
    private final String instanceId;
    private int attack;
    private int defense;
    private int currentDefense;
    private int cost;

    public Card(
            String instanceId,
            String cardId,
            int attack,
            int defense,
            int cost) {
        this.instanceId = instanceId;
        this.cardId = cardId;
        this.attack = attack;
        this.defense = defense;
        this.cost = cost;
        this.currentDefense = defense;
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

    public int getCurrentDefense() {
        return currentDefense;
    }

    public int getCost() {
        return cost;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void takeDamage(int damage) {

        currentDefense -= damage;

    }

    public boolean isDestroyed() {

        return currentDefense <= 0;

    }

}
