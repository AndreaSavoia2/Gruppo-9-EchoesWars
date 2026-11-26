package it.echoeswar.management.backoffice.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

/**
 * Request payload used to create a new card.
 *
 * <p>The faction, school and tier are referenced by identifier: the service
 * resolves them and rejects the request if one of them does not exist.</p>
 *
 * @param nameIt         card name in Italian
 * @param nameEn         card name in English
 * @param descriptionIt  card description in Italian (optional)
 * @param descriptionEn  card description in English (optional)
 * @param attack         attack value of the card
 * @param defense        defense value of the card
 * @param playCost       cost required to play the card
 * @param artUrl         URL of the card artwork (optional)
 * @param echoesId       identifier of the faction the card belongs to
 * @param schoolId       identifier of the school the card belongs to
 * @param tierId         identifier of the tier associated with the card
 */
@Builder
@Schema(
        name = "CardRequest",
        description = "Payload for creating a card, referencing faction, school and tier by identifier"
)
public record CardRequest(

        @Schema(description = "Card name in Italian", example = "Guardiano",
        requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String nameIt,

        @Schema(description = "Card name in English", example = "Guardian",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String nameEn,

        @Schema(description = "Card description in Italian, when available",
                example = "Protegge gli alleati", nullable = true)
        @Size(max = 5000)
        String descriptionIt,

        @Schema(description = "Card description in English, when available",
                example = "Protects allies", nullable = true)
        @Size(max = 5000)
        String descriptionEn,

        @Schema(description = "Attack value of the card", example = "8",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(127)
        Byte attack,

        @Schema(description = "Defense value of the card", example = "12",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(127)
        Byte defense,

        @Schema(description = "Play cost of the card", example = "6",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(127)
        Byte playCost,

        @Schema(description = "Max number of one card in the same deck", example = "3",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(3)
        Byte maxCopyDeck,

        @Schema(description = "URL of the card artwork, when available",
                example = "https://cdn.echoeswar.it/cards/dominion-001.png",
                format = "uri",
                nullable = true)
        @URL
        @Size(max = 500)
        String artUrl,

        @Schema(description = "Identifier of the faction to which the card belong", example = "2",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(127)
        Byte echoesId,

        @Schema(description = "Identifier of the school to which the card belong", example = "2",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(127)
        Byte schoolId,

        @Schema(description = "Identifier of the tier to which the card belong", example = "2",
                format = "int32", minimum = "0", maximum = "127",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(127)
        Byte tierId
) {
}
