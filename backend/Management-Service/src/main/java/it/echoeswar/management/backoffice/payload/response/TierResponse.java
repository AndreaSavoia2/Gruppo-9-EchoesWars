package it.echoeswar.management.backoffice.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.Tier;
import it.echoeswar.management.catalog.enumerated.TierCode;
import it.echoeswar.management.common.utils.FormatterString;
import lombok.Builder;

/**
 * Public backoffice representation of a {@link Tier}.
 *
 * @param id       unique tier identifier
 * @param code     unique tier code
 * @param nameIt   tier name in Italian, with the first letter capitalized
 * @param nameEn   tier name in English, with the first letter capitalized
 * @param level    numeric level of the tier (minimum 1)
 * @param imageUrl URL of the tier image
 */
@Builder
@Schema(
        name = "TierResponse",
        description = "Public backoffice representation of a tier."
)
public record TierResponse(
        @Schema(
                description = "Unique tier identifier.",
                example = "1",
                format = "int32",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte id,

        @Schema(
                description = "Unique code identifier.",
                example = "#CD_02",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        TierCode code,

        @Schema(
                description = "Tier name in Italian.",
                example = "Comune",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameIt,

        @Schema(
                description = "Tier name in English.",
                example = "Common",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameEn,

        @Schema(
                description = "Numeric level of the tier.",
                example = "1",
                format = "int32",
                minimum = "1",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int level,

        @Schema(
                description = "Image URL of the tier.",
                example = "https://example.com/images/tiers/common.png",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String imageUrl
) {

    /**
     * Creates a {@link TierResponse} from a {@link Tier} entity.
     * <p>
     * The Italian and English names are returned with the first letter capitalized.
     *
     * @param tier the tier entity to convert
     * @return the corresponding response object
     */
    public static TierResponse from(Tier tier) {
        return TierResponse.builder()
                .id(tier.getId())
                .code(tier.getCode())
                .nameIt(FormatterString.capitalizeFirstLetter(tier.getNameIt()))
                .nameEn(FormatterString.capitalizeFirstLetter(tier.getNameEn()))
                .level(tier.getLevel())
                .imageUrl(tier.getImageUrl())
                .build();
    }
}