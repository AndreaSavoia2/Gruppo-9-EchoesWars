package com.echoeswars.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Player {

    private String playerId;
    private int health;
    private ArrayList<Card> deck;
    private Card activeCard;

    public Player(String playerId, ArrayList<Card> deck) {
        this.playerId = playerId;
        this.health = 10;
        this.deck = deck;
        this.activeCard = null;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getHealth() {
        return health;
    }

    public ArrayList<Card> getDeck() {
        return new ArrayList<>(deck);
    }

    public void shuffleDeck(Random random) {
        Collections.shuffle(deck, random);
    }

    public Card getActiveCard() {
        return activeCard;
    }

    public void drawCard() {

        if (deck.isEmpty()) {
            System.out.println("Cannot draw: deck is empty.");
            return;
        }

        activeCard = deck.remove(0);
    }

    public void takeDamage(int damage) {

        health -= damage;

    }

    public void replaceDestroyedCard() {

        // Keep the active card if it survived
        if (activeCard == null || !activeCard.isDestroyed()) {
            return;
        }

        // Remove the destroyed card
        activeCard = null;

        // Draw a replacement from the deck
        if (!deck.isEmpty()) {
            drawCard();
        }
    }

    public boolean canContinue() {

        // A player needs positive HP and a surviving active card
        return health > 0
                && activeCard != null
                && !activeCard.isDestroyed();

    }
}
