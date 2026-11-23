package it.echoeswar.management.deck.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/** Data supplied by an authenticated user to create a complete deck. */
@Schema(
        name = "CreateDeckRequest",
        description = "Data used to create a deck under an active deck rule and explicitly selected factions."
)
public record CreateDeckRequest(

        @Schema(
                description = "Unique deck name for the authenticated user.",
                example = "Fiamme e Ombre",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Deck name must not be blank.")
        @Size(max = 100, message = "Deck name must not exceed 100 characters.")
        String name,

        @Schema(
                description = "The distinct factions assigned to the deck. Their number is defined by the selected deck rule; every selected card must belong to one of them.",
                example = "[1, 2]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Deck factions must be provided.")
        @NotEmpty(message = "Deck must contain at least one faction.")
        List<@NotNull @Positive Byte> factionIds,

        @Schema(
                description = "Distinct cards and their quantities. Each card must belong to one of the selected deck factions.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Deck cards must be provided.")
        @NotEmpty(message = "Deck must contain at least one card entry.")
        List<@NotNull @Valid DeckCardRequest> cards
) {
}
