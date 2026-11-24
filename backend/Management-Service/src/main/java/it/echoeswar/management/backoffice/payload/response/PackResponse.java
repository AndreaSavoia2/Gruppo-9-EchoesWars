package it.echoeswar.management.backoffice.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.pack.entity.GamePackage;
import lombok.Builder;

/**
 * Response payload returned by the back-office after a pack has been
 * created or retrieved.
 *
 * <p>Instances are built from a {@link GamePackage} entity through
 * {@link #from(GamePackage)}. Textual fields are returned with the first
 * letter capitalized.</p>
 *
 * @param id            unique pack identifier
 * @param code          unique pack code
 * @param nameIt        pack name in Italian
 * @param nameEn        pack name in English
 * @param descriptionIt pack description in Italian, may be {@code null}
 * @param descriptionEn pack description in English, may be {@code null}
 */
@Builder
@Schema(
        name = "PackResponse",
        description = "Representation of a pack returned by the back-office API, "
                + "including its identifier, unique code, localized names "
                + "and optional localized descriptions"
)
public record PackResponse(
        @Schema(
                description = "Unique pack identifier.",
                example = "10",
                format = "int64",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        long id,

        @Schema(
                description = "Unique pack code.",
                example = "#123456",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String code,

        @Schema(
                description = "Pack name in Italian.",
                example = "Pacchetto settimanale",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameIt,

        @Schema(
                description = "Pack name in English.",
                example = "Weekly pack",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameEn,

        @Schema(
                description = "Pack description in Italian, when available.",
                example = "Pacchetto disponibile ogni settimana.",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String descriptionIt,

        @Schema(
                description = "Pack description in English, when available.",
                example = "Available pack every week.",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String descriptionEn
) {

    /**
     * Creates a {@link PackResponse} from a {@link GamePackage} entity.
     *
     * <p>Every textual field is passed through
     * {@link FormatterString#capitalizeFirstLetter(String)} before being
     * returned.</p>
     *
     * @param gamePackage the entity to convert
     * @return the response representing the given pack
     */
    public static PackResponse from(GamePackage gamePackage) {
        return PackResponse.builder()
                .id(gamePackage.getId())
                .code(FormatterString.capitalizeFirstLetter(gamePackage.getCode()))
                .nameIt(FormatterString.capitalizeFirstLetter(gamePackage.getNameIt()))
                .nameEn(FormatterString.capitalizeFirstLetter(gamePackage.getNameEn()))
                .descriptionIt(FormatterString.capitalizeFirstLetter(gamePackage.getDescriptionIt()))
                .descriptionEn(FormatterString.capitalizeFirstLetter(gamePackage.getDescriptionEn()))
                .build();
    }
}