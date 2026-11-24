package com.echoeswars.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.List;

import com.echoeswars.model.CardSchool;

public class Player {

    private String playerId;
    private int health;
    private ArrayList<Card> deck;
    private Card activeCard;
    // Cards discarded or destroyed during the match
    private final List<Card> discardPile = new ArrayList<>();
    // School of the most recent allied card destroyed on the field
    private CardSchool lastDestroyedSchool = null;

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

    /**
     * Discards the top card of the deck.
     *
     * An empty deck does not cause a loss when the discard
     * is triggered by an effect.
     *
     * @return the discarded card, or null if the deck is empty
     */
    public Card discardTopCard() {

        if (deck.isEmpty()) {
            return null;
        }

        Card discardedCard = deck.remove(0);
        discardPile.add(discardedCard);

        return discardedCard;
    }

    /**
     * Registers a card destroyed while on the battlefield.
     *
     * This updates the last destroyed school, unlike
     * discarding a card directly from the deck.
     */
    public void registerDestroyedCard(Card card) {

        if (card == null) {
            throw new IllegalArgumentException(
                    "Destroyed card cannot be null.");
        }

        discardPile.add(card);
        lastDestroyedSchool = card.getSchool();
    }

    /**
     * Returns a read-only view of the discard pile.
     */
    public List<Card> getDiscardPile() {
        return Collections.unmodifiableList(discardPile);
    }

    public int getDiscardPileSize() {
        return discardPile.size();
    }

    public CardSchool getLastDestroyedSchool() {
        return lastDestroyedSchool;
    }

    /**
     * Restores health without exceeding the maximum of 20.
     *
     * This method does not revive defeated players.
     *
     * @return the number of health points actually restored
     */
    public int heal(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Healing amount cannot be negative.");
        }

        if (health <= 0) {
            return 0;
        }

        int previousHealth = health;

        health = Math.min(20, health + amount);

        return health - previousHealth;
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
