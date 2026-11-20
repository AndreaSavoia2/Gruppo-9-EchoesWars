package it.echoeswar.management.deck.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.deck.enumerated.DeckStatus;
import lombok.Builder;

import java.util.List;

/**
 * Full representation of one deck, including its distinct card entries.
 *
 * <p>Each card appears once and its {@link CardResponse#quantity()} is the number of copies
 * assigned to this deck.</p>
 */
@Builder
@Schema(
        name = "DeckDetailResponse",
        description = "A deck with its rule summary, selected factions, and distinct cards."
)
public record DeckDetailResponse(

        @Schema(description = "Deck identifier.", example = "42", format = "int64")
        long id,

        @Schema(description = "Applied deck-rule identifier.", example = "1", format = "int64")
        long deckRuleId,

        @Schema(description = "Applied deck-rule code.", example = "STANDARD_30")
        String deckRuleCode,

        @Schema(description = "Deck name.", example = "Fiamme e Ombre")
        String name,

        @Schema(description = "Deck lifecycle state.", example = "READY")
        DeckStatus status,

        @Schema(description = "Total number of cards in the deck.", example = "30", format = "int32")
        int totalCards,

        @Schema(description = "Total play cost of all cards in the deck.", example = "86", format = "int32")
        int totalCost,

        @Schema(
                description = "The factions explicitly associated with the deck, including their Italian and English names."
        )
        List<DeckFactionResponse> factions,

        @Schema(
                description = "Distinct catalog cards in the deck. Each card contains its quantity in this deck."
        )
        List<CardResponse> cards
) {
}
