package it.echoeswar.management.pack.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.pack.entity.GamePackage;
import lombok.Builder;

/**
 * Public representation of an available game package.
 *
 * @param id            unique package identifier
 * @param code          unique package code
 * @param nameIt        package name in Italian
 * @param nameEn        package name in English
 * @param descriptionIt package description in Italian, when available
 * @param descriptionEn package description in English, when available
 */
@Builder
@Schema(
        name = "GamePackageResponse",
        description = "Catalog representation of an available game package."
)
public record GamePackageResponse(
        long id,
        String code,
        String nameIt,
        String nameEn,
        String descriptionIt,
        String descriptionEn
) {

    /**
     * Maps a package catalogue entity to its API representation.
     *
     * @param gamePackage package entity to map
     * @return the corresponding response DTO
     */
    public static GamePackageResponse from(GamePackage gamePackage) {
        return GamePackageResponse.builder()
                .id(gamePackage.getId())
                .code(gamePackage.getCode())
                .nameIt(gamePackage.getNameIt())
                .nameEn(gamePackage.getNameEn())
                .descriptionIt(gamePackage.getDescriptionIt())
                .descriptionEn(gamePackage.getDescriptionEn())
                .build();
    }
}
