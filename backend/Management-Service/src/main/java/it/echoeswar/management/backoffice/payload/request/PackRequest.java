package it.echoeswar.management.backoffice.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request payload used by the back-office to create a new pack.
 *
 * <p>Code and names are mandatory, while descriptions are optional.
 * All fields are validated against the maximum lengths allowed by the
 * database schema.</p>
 *
 * @param code          unique pack code (required, max 100 characters)
 * @param nameIt        pack name in Italian (required, max 100 characters)
 * @param nameEn        pack name in English (required, max 100 characters)
 * @param descriptionIt pack description in Italian, may be {@code null}
 *                      (max 250 characters)
 * @param descriptionEn pack description in English, may be {@code null}
 *                      (max 250 characters)
 */
@Builder
@Schema(
        name = "PackRequest",
        description = "Payload for creating a pack"
)
public record PackRequest(
        @Schema(description = "Unique pack code", example = "#1234_001",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String code,

        @Schema(description = "Pack name in Italian", example = "Pacchetto settimanale",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String nameIt,

        @Schema(description = "Pack name in English", example = "Weekly pack",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String nameEn,

        @Schema(description = "Pack description in Italian, when available",
                example = "Pacchetto disponibile ogni settimana", nullable = true)
        @Size(max = 250)
        String descriptionIt,

        @Schema(description = "Pack description in English, when available",
                example = "Available pack every week", nullable = true)
        @Size(max = 250)
        String descriptionEn
) {

}