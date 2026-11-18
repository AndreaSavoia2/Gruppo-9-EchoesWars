package it.echoeswar.management.deck.model;

import it.echoeswar.management.catalog.entity.Card;

public record ResolvedDeckCard(Card card, int quantity) {
}
