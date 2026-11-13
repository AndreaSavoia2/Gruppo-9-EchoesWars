package it.echoeswar.management.collection.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.collection.entity.UserCard;
import it.echoeswar.management.common.utils.FormatterString;
import lombok.Builder;

/**
 * Response payload for a card owned by the authenticated user.
 *
 * <p>It combines flattened catalog data with the number of copies owned by
 * that user. Presentation text mapped from the card is normalized through
 * {@link FormatterString#capitalizeFirstLetter(String)} to keep collection
 * responses consistent with catalog responses.</p>
 *
 * @param id             unique identifier of the card
 * @param code           unique code of the card
 * @param nameIt         card name in Italian
 * @param nameEn         card name in English
 * @param descriptionIt  card description in Italian
 * @param descriptionEn  card description in English
 * @param attack         attack value of the card
 * @param defense        defense value of the card
 * @param cost           cost required to play the card
 * @param artUrl         URL of the card artwork
 * @param frameImageUrl  URL of the frame image associated with the card tier
 * @param echoesId       identifier of the faction the card belongs to
 * @param echoesNameIt   faction name in Italian
 * @param echoesNameEn   faction name in English
 * @param schoolId       identifier of the school the card belongs to
 * @param schoolNameIt   school name in Italian
 * @param schoolNameEn   school name in English
 * @param tierLevel      level of the tier associated with the card
 * @param quantity       number of copies of the card owned by the user
 */
@Builder
@Schema(
        name = "CollectionResponse",
        description = "Card details in the authenticated user's collection, including the owned quantity."
)
public record CollectionResponse(
        @Schema(
                description = "Unique card identifier.",
                example = "101",
                format = "int64",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        long id,

        @Schema(
                description = "Unique catalog code of the card.",
                example = "DOMINION_001",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String code,

        @Schema(
                description = "Card name in Italian.",
                example = "Guardiano",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameIt,

        @Schema(
                description = "Card name in English.",
                example = "Guardian",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nameEn,

        @Schema(
                description = "Card description in Italian, when available.",
                example = "Protegge gli alleati.",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String descriptionIt,

        @Schema(
                description = "Card description in English, when available.",
                example = "Protects allies.",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String descriptionEn,

        @Schema(
                description = "Attack value of the card.",
                example = "4",
                format = "int32",
                minimum = "0",
                maximum = "127",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte attack,

        @Schema(
                description = "Defense value of the card.",
                example = "6",
                format = "int32",
                minimum = "0",
                maximum = "127",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte defense,

        @Schema(
                description = "Cost required to play the card.",
                example = "3",
                format = "int32",
                minimum = "1",
                maximum = "10",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte cost,

        @Schema(
                description = "URL of the card artwork, when available.",
                example = "https://cdn.echoeswar.it/cards/dominion-001.png",
                format = "uri",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String artUrl,

        @Schema(
                description = "URL of the frame image associated with the card tier, when available.",
                example = "https://cdn.echoeswar.it/tiers/common-frame.png",
                format = "uri",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String frameImageUrl,

        @Schema(
                description = "Identifier of the faction to which the card belongs.",
                example = "1",
                format = "int32",
                minimum = "1",
                maximum = "127",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte echoesId,

        @Schema(
                description = "Faction name in Italian.",
                example = "Dominio",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String echoesNameIt,

        @Schema(
                description = "Faction name in English.",
                example = "Dominion",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String echoesNameEn,

        @Schema(
                description = "Identifier of the school to which the card belongs.",
                example = "1",
                format = "int32",
                minimum = "1",
                maximum = "127",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        byte schoolId,

        @Schema(
                description = "School name in Italian.",
                example = "Guerriero",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String schoolNameIt,

        @Schema(
                description = "School name in English.",
                example = "Warrior",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String schoolNameEn,

        @Schema(
                description = "Numeric level of the card tier.",
                example = "1",
                format = "int32",
                minimum = "1",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int tierLevel,

        @Schema(
                description = "Number of copies of this card owned by the authenticated user.",
                example = "2",
                format = "int32",
                minimum = "1",
                accessMode = Schema.AccessMode.READ_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int quantity
) {
    /**
     * Builds a collection response from a user's card-ownership record.
     *
     * <p>The related card, faction, school, and tier are flattened to avoid
     * exposing persistence entities in the API response.</p>
     *
     * @param userCard the ownership record to convert; its card and the card's
     *                 faction, school, and tier must be available
     * @return a new {@code CollectionResponse} populated from the ownership
     *         record and its card
     */
    public static CollectionResponse from(UserCard userCard) {
        Card card = userCard.getCard();
        return CollectionResponse.builder()
                .id(userCard.getCard().getId())
                .code(FormatterString.capitalizeFirstLetter(card.getCode()))
                .nameIt(FormatterString.capitalizeFirstLetter(card.getNameIt()))
                .nameEn(FormatterString.capitalizeFirstLetter(card.getNameEn()))
                .descriptionIt(FormatterString.capitalizeFirstLetter(card.getDescriptionIt()))
                .descriptionEn(FormatterString.capitalizeFirstLetter(card.getDescriptionEn()))
                .attack(card.getAttack())
                .defense(card.getDefense())
                .cost(card.getPlayCost())
                .artUrl(FormatterString.capitalizeFirstLetter(card.getImageUrl()))
                .frameImageUrl(card.getTier().getImageUrl())
                .echoesId(card.getFaction().getId())
                .echoesNameIt(FormatterString.capitalizeFirstLetter(card.getFaction().getNameIt()))
                .echoesNameEn(FormatterString.capitalizeFirstLetter(card.getFaction().getNameEn()))
                .schoolId(card.getSchool().getId())
                .schoolNameIt(FormatterString.capitalizeFirstLetter(card.getSchool().getNameIt()))
                .schoolNameEn(FormatterString.capitalizeFirstLetter(card.getSchool().getNameEn()))
                .tierLevel(card.getTier().getLevel())
                .quantity(userCard.getQuantity())
                .build();
    }
}
