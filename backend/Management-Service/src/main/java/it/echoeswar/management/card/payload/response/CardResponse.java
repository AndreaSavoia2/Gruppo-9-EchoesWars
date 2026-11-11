package it.echoeswar.management.card.payload.response;

import it.echoeswar.management.card.entity.Card;

import java.time.LocalDateTime;

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
        LocalDateTime created,
        LocalDateTime updated,

        //faction
        byte factionId,
        String factionNameIt,
        String factionNameEn,

        //school
        byte schoolId,
        String schoolNameIt,
        String schoolNameEn,
        String tiarLevel,
        boolean tiarEnabled,
        LocalDateTime tiarCreated,
        LocalDateTime tiarUpdated
) {
    public static CardResponse from(Card card){
        return new CardResponse(
                card.getId(),
                card.getCode(),
                card.getNameIt(),
                card.getNameEn(),
                card.getDescriptionIt(),
                card.getDescriptionEn(),
                card.getAttack(),
                card.getAttack(),
                card.getDefense(),
                card.getCost(),
                card.getArtUrl(),
                card.isEnabled(),
                card.getCreated(),
                card.getUpdated(),

                card.getFaction().getId(),
                card.getFaction().getNameIt(),
                card.getFaction().getNameEn(),

                card.getSchool().getId(),
                card.getSchool().getNameIt(),
                card.getSchool().getNameEn(),
                card.getSchool().getArtUrl(),

                card.getTiar().getId(),
                card.getTiar().getCode(),
                card.getTiar().getNameIt(),
                card.getTiar().getNameEn(),
                card.getTiar().getLevel().name(),
                card.getTiar().isEnabled(),
                card.getTiar().getCreated(),
                card.getTiar().getUpdated()
        );
    }
}
