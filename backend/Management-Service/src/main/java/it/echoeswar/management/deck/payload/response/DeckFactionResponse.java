package it.echoeswar.management.deck.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.common.utils.FormatterString;

/**
 * Represents one of the factions explicitly associated with a deck.
 */
@Schema(
        name = "DeckFactionResponse",
        description = "An explicitly selected deck faction, including its localized names."
)
public record DeckFactionResponse(

        @Schema(description = "Faction identifier.", example = "1", format = "int32")
        byte id,

        @Schema(description = "Faction name in Italian.", example = "Fiamma")
        String nameIt,

        @Schema(description = "Faction name in English.", example = "Flame")
        String nameEn
) {

    /**
     * Converts the catalog entity into the format exposed by deck responses.
     */
    public static DeckFactionResponse from(Faction faction) {
        return new DeckFactionResponse(
                faction.getId(),
                FormatterString.capitalizeFirstLetter(faction.getNameIt()),
                FormatterString.capitalizeFirstLetter(faction.getNameEn())
        );
    }
}
