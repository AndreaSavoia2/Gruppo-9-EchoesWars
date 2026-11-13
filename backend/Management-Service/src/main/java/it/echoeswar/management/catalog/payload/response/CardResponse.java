package it.echoeswar.management.catalog.payload.response;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.common.utils.FormatterString;
import lombok.Builder;

/**
 * Response payload representing a card together with its related faction,
 * school and tiar information.
 *
 * <p>Text fields are returned with the first letter capitalized
 * (see {@link FormatterString#capitalizeFirstLetter(String)}).</p>
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
 * @param echoesId      identifier of the faction the card belongs to
 * @param echoesNameIt  faction name in Italian
 * @param echoesNameEn  faction name in English
 * @param schoolId       identifier of the school the card belongs to
 * @param schoolNameIt   school name in Italian
 * @param schoolNameEn   school name in English
 * @param tierLevel      level of the tiar associated with the card
 */
@Builder
public record CardResponse(
        //card
        long id,
        String code,
        String nameIt,
        String nameEn,
        String descriptionIt,
        String descriptionEn,
        byte attack,
        byte defense,
        byte cost,
        String artUrl,

        //faction
        byte echoesId,
        String echoesNameIt,
        String echoesNameEn,

        //school
        byte schoolId,
        String schoolNameIt,
        String schoolNameEn,
        int tierLevel
) {
    /**
     * Builds a {@link CardResponse} from a {@link Card} entity, flattening the
     * related faction, school and tiar data into a single object.
     *
     * @param card the card entity to convert; its faction, school and tiar
     *             must not be {@code null}
     * @return a new {@code CardResponse} populated from the given entity
     */
    public static CardResponse from(Card card){
        return CardResponse.builder()
                .id(card.getId())
                .code(FormatterString.capitalizeFirstLetter(card.getCode()))
                .nameIt(FormatterString.capitalizeFirstLetter(card.getNameIt()))
                .nameEn(FormatterString.capitalizeFirstLetter(card.getNameEn()))
                .descriptionIt(FormatterString.capitalizeFirstLetter(card.getDescriptionIt()))
                .descriptionEn(FormatterString.capitalizeFirstLetter(card.getDescriptionEn()))
                .attack(card.getAttack())
                .defense(card.getDefense())
                .cost(card.getPlayCost())
                .artUrl(FormatterString.capitalizeFirstLetter(card.getImageUrl()))
                .echoesId(card.getFaction().getId())
                .echoesNameIt(FormatterString.capitalizeFirstLetter(card.getFaction().getNameIt()))
                .echoesNameEn(FormatterString.capitalizeFirstLetter(card.getFaction().getNameEn()))
                .schoolId(card.getSchool().getId())
                .schoolNameIt(FormatterString.capitalizeFirstLetter(card.getSchool().getNameIt()))
                .schoolNameEn(FormatterString.capitalizeFirstLetter(card.getSchool().getNameEn()))
                .tierLevel(card.getTier().getLevel())
                .build();
    }
}
