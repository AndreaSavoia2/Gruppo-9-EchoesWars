package it.echoeswar.management.deck.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Editable data of an existing deck.
 *
 * <p>Factions are not part of the request: the factions associated with the deck
 * at creation time remain unchanged.</p>
 */
@Schema(
        name = "UpdateDeckRequest",
        description = "Data used to replace the editable data and complete card composition of an existing deck."
)
public record UpdateDeckRequest(

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
                description = "Complete replacement of the distinct cards and their quantities. Each card must belong to a faction already assigned to the deck.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Deck cards must be provided.")
        @NotEmpty(message = "Deck must contain at least one card entry.")
        List<@NotNull @Valid DeckCardRequest> cards
) {
}
