package it.echoeswar.management.card.payload.response;

import it.echoeswar.management.card.entity.Card;

import it.echoeswar.management.common.utils.FormatterString;

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
 * @param enabled        whether the card is enabled
 * @param factionId      identifier of the faction the card belongs to
 * @param factionNameIt  faction name in Italian
 * @param factionNameEn  faction name in English
 * @param schoolId       identifier of the school the card belongs to
 * @param schoolNameIt   school name in Italian
 * @param schoolNameEn   school name in English
 * @param tiarLevel      level of the tiar associated with the card
 * @param tiarEnabled    whether the tiar associated with the card is enabled
 */
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
        boolean enabled,

        //faction
        byte factionId,
        String factionNameIt,
        String factionNameEn,

        //school
        byte schoolId,
        String schoolNameIt,
        String schoolNameEn,
        String tiarLevel,
        boolean tiarEnabled
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
        return new CardResponse(
                card.getId(),
                FormatterString.capitalizeFirstLetter(card.getCode()),
                FormatterString.capitalizeFirstLetter(card.getNameIt()),
                FormatterString.capitalizeFirstLetter(card.getNameEn()),
                FormatterString.capitalizeFirstLetter(card.getDescriptionIt()),
                FormatterString.capitalizeFirstLetter(card.getDescriptionEn()),
                card.getAttack(),
                card.getAttack(),
                card.getDefense(),
                card.getCost(),
                FormatterString.capitalizeFirstLetter(card.getArtUrl()),
                card.isEnabled(),

                card.getFaction().getId(),
                FormatterString.capitalizeFirstLetter(card.getFaction().getNameIt()),
                FormatterString.capitalizeFirstLetter(card.getFaction().getNameEn()),

                card.getSchool().getId(),
                FormatterString.capitalizeFirstLetter(card.getSchool().getNameIt()),
                FormatterString.capitalizeFirstLetter(card.getSchool().getNameEn()),
                FormatterString.capitalizeFirstLetter(card.getSchool().getArtUrl()),

                card.getTiar().getId(),
                FormatterString.capitalizeFirstLetter(card.getTiar().getCode()),
                FormatterString.capitalizeFirstLetter(card.getTiar().getNameIt()),
                FormatterString.capitalizeFirstLetter(card.getTiar().getNameEn()),
                FormatterString.capitalizeFirstLetter(card.getTiar().getLevel().name()),
                card.getTiar().isEnabled()
        );
    }
}
