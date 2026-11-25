package it.echoeswar.management.backoffice.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.School;
import it.echoeswar.management.catalog.enumerated.SchoolCode;
import it.echoeswar.management.common.utils.FormatterString;
import lombok.Builder;

/**
 * Public backoffice representation of a {@link School}.
 *
 * @param id       unique school identifier
 * @param code     unique school code
 * @param nameIt   school name in Italian, with the first letter capitalized
 * @param nameEn   school name in English, with the first letter capitalized
 * @param imageUrl URL of the school image
 */
@Builder
@Schema(
        name = "SchoolResponse",
        description = "Public backoffice representation of a school."
)
public record SchoolResponse(
        @Schema(
                description = "Unique school identifier.",
                example = "1",
                format = "int32",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte id,

        @Schema(
                description = "Unique code identifier.",
                example = "WARRIOR",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        SchoolCode code,

        @Schema(
                description = "School name in Italian.",
                example = "GUERRIERI",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameIt,

        @Schema(
                description = "School name in English.",
                example = "WARRIOR",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameEn,

        @Schema(
                description = "Image URL of the school.",
                example = "https://example.com/images/schools/warrior.png",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String imageUrl
) {

    /**
     * Creates a {@link SchoolResponse} from a {@link School} entity.
     * <p>
     * The Italian and English names are returned with the first letter capitalized.
     *
     * @param school the school entity to convert
     * @return the corresponding response object
     */
    public static SchoolResponse from(School school) {
        return SchoolResponse.builder()
                .id(school.getId())
                .code(school.getCode())
                .nameIt(FormatterString.capitalizeFirstLetter(school.getNameIt()))
                .nameEn(FormatterString.capitalizeFirstLetter(school.getNameEn()))
                .imageUrl(school.getImageUrl())
                .build();
    }
}