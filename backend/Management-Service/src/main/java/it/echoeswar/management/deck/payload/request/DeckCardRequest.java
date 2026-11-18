package it.echoeswar.management.deck.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** One catalog-card quantity selected for a deck. */
@Schema(
        name = "DeckCardRequest",
        description = "A catalog card and the number of copies to include in the deck."
)
public record DeckCardRequest(

        @Schema(
                description = "Identifier of a card owned by the authenticated user.",
                example = "101",
                format = "int64",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Card id must be provided.")
        @Positive(message = "Card id must be positive.")
        Long cardId,

        @Schema(
                description = "Number of copies of this card to include.",
                example = "3",
                format = "int32",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Card quantity must be provided.")
        @Positive(message = "Card quantity must be positive.")
        Integer quantity
) {
}
