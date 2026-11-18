package it.echoeswar.management.deck.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.deck.enumerated.DeckStatus;
import lombok.Builder;

import java.util.List;

/** Confirmation returned after a deck has been created successfully. */
@Builder
@Schema(
        name = "DeckResponse",
        description = "A newly created deck together with the rule-derived composition summary."
)
public record DeckResponse(

        @Schema(description = "Deck identifier.", example = "42", format = "int64")
        long id,

        @Schema(description = "Applied deck-rule identifier.", example = "1", format = "int64")
        long deckRuleId,

        @Schema(description = "Applied deck-rule code.", example = "STANDARD_30")
        String deckRuleCode,

        @Schema(description = "Deck name.", example = "Fiamme e Ombre")
        String name,

        @Schema(description = "Optional deck description.", nullable = true)
        String description,

        @Schema(description = "Deck lifecycle state.", example = "READY")
        DeckStatus status,

        @Schema(description = "Total number of cards in the deck.", example = "30", format = "int32")
        int totalCards,

        @Schema(description = "Total play cost of all cards in the deck.", example = "86", format = "int32")
        int totalCost,

        @Schema(description = "The factions explicitly assigned to the deck.", example = "[1, 2]")
        List<Byte> factionIds,

        @Schema(description = "Distinct catalog cards and their quantities in the deck.")
        List<DeckCardResponse> cards
) {
}
