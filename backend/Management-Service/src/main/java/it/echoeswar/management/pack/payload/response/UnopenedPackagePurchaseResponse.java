package it.echoeswar.management.pack.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.pack.entity.PackagePurchase;

import java.time.LocalDateTime;

/**
 * A package purchase that has not yet been opened by its owner.
 */
@Schema(
        name = "UnopenedPackagePurchaseResponse",
        description = "A package purchase belonging to the authenticated user that has not yet been opened."
)
public record UnopenedPackagePurchaseResponse(
        @Schema(description = "Unique purchase identifier.", example = "42")
        long purchaseId,

        @Schema(description = "Purchased catalog package.")
        GamePackageResponse gamePackage,

        @Schema(description = "Identifier of the purchased package version.", example = "7")
        long packageVersionId,

        @Schema(description = "Version number of the purchased package.", example = "2")
        int versionNumber,

        @Schema(description = "Credits paid for this purchase.", example = "100")
        long pricePaidCredits,

        @Schema(
                description = "Date and time at which the package was purchased.",
                example = "2026-11-17T10:15:30",
                format = "date-time"
        )
        LocalDateTime purchasedAt
) {

    /**
     * Maps an unopened purchase to its API representation.
     *
     * @param packagePurchase persisted unopened package purchase
     * @return response payload for the unopened purchase
     */
    public static UnopenedPackagePurchaseResponse from(PackagePurchase packagePurchase) {
        return new UnopenedPackagePurchaseResponse(
                packagePurchase.getId(),
                GamePackageResponse.from(packagePurchase.getPackageVersion().getGamePackage()),
                packagePurchase.getPackageVersion().getId(),
                packagePurchase.getPackageVersion().getVersionNumber(),
                packagePurchase.getPricePaidCredits(),
                packagePurchase.getPurchasedAt()
        );
    }
}
