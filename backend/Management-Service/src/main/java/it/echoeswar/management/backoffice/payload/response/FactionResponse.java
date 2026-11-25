package it.echoeswar.management.backoffice.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.catalog.enumerated.EchoesCode;
import it.echoeswar.management.common.utils.FormatterString;
import lombok.Builder;

/**
 * Public backoffice representation of a faction.
 *
 * @param id       unique faction identifier
 * @param code     unique faction code
 * @param nameIt   faction name in Italian
 * @param nameEn   faction name in English
 * @param colorHex faction color as a hexadecimal string (e.g. {@code #FF5733})
 */
@Builder
@Schema(
        name = "FactionResponse",
        description = "Public backoffice representation of a faction."
)
public record FactionResponse(
        @Schema(
                description = "Unique faction identifier.",
                example = "1",
                format = "int32",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte id,

        @Schema(
                description = "Unique code identifier.",
                example = "DOMINIO",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        EchoesCode code,

        @Schema(
                description = "Faction name in Italian.",
                example = "DOMINIO",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameIt,

        @Schema(
                description = "Faction name in English.",
                example = "DOMINION",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameEn,

        @Schema(
                description = "Hexadecimal color of the faction.",
                example = "#FF5733",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String colorHex
) {

    /**
     * Creates a {@link FactionResponse} from a {@link Faction} entity.
     * <p>
     * The Italian and English names are returned with the first letter capitalized.
     *
     * @param faction the faction entity to convert
     * @return the corresponding response object
     */
    public static FactionResponse from(Faction faction) {
        return FactionResponse.builder()
                .id(faction.getId())
                .code(faction.getCode())
                .nameIt(FormatterString.capitalizeFirstLetter(faction.getNameIt()))
                .nameEn(FormatterString.capitalizeFirstLetter(faction.getNameEn()))
                .colorHex(faction.getColorHex())
                .build();
    }
}