package it.echoeswar.management.deck.model;



import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.deck.service.DeckService;

import java.util.List;
import java.util.Map;

public record DeckComposition(
        int totalCards,
        int totalCost,
        Map<Byte, Faction> selectedFactionsById,
        List<ResolvedDeckCard> cards
) {
}
