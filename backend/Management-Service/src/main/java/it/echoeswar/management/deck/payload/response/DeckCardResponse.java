package it.echoeswar.management.deck.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** A persisted card entry in a newly created deck. */
@Schema(
        name = "DeckCardResponse",
        description = "A card and its quantity in a deck."
)
public record DeckCardResponse(

        @Schema(description = "Catalog-card identifier.", example = "101", format = "int64")
        long cardId,

        @Schema(description = "Faction inferred from the catalog card.", example = "1", format = "int32")
        byte factionId,

        @Schema(description = "Copies of this card in the deck.", example = "3", format = "int32")
        int quantity
) {
}
