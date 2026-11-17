package it.echoeswar.management.pack.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.echoeswar.management.pack.entity.PackagePurchase;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Confirmation returned after a package purchase is completed.
 *
 * @param purchaseId          unique purchase identifier
 * @param packageId           identifier of the purchased catalog package
 * @param packageVersionId    identifier of the commercial version used for the purchase
 * @param packageCode         code of the purchased package
 * @param versionNumber       commercial version number used for the purchase
 * @param pricePaidCredits    immutable amount debited for the purchase
 * @param remainingBalance    wallet balance after the debit
 * @param creditTransactionId identifier of the recorded credit transaction
 * @param purchasedAt         time at which the purchase was persisted
 * @param opened              whether the package has already been opened
 */
@Builder
@Schema(
        name = "PackagePurchaseResponse",
        description = "Confirmation of a package purchase that is still unopened."
)
public record PackagePurchaseResponse(
        long purchaseId,
        long packageId,
        long packageVersionId,
        String packageCode,
        int versionNumber,
        long pricePaidCredits,
        long remainingBalance,
        long creditTransactionId,
        LocalDateTime purchasedAt,
        boolean opened
) {

    /**
     * Maps a newly created, unopened purchase to its API representation.
     *
     * @param packagePurchase persisted package purchase
     * @param remainingBalance balance available after the debit
     * @return purchase confirmation DTO
     */
    public static PackagePurchaseResponse from(
            PackagePurchase packagePurchase,
            long remainingBalance
    ) {
        return PackagePurchaseResponse.builder()
                .purchaseId(packagePurchase.getId())
                .packageId(packagePurchase.getPackageVersion().getGamePackage().getId())
                .packageVersionId(packagePurchase.getPackageVersion().getId())
                .packageCode(packagePurchase.getPackageVersion().getGamePackage().getCode())
                .versionNumber(packagePurchase.getPackageVersion().getVersionNumber())
                .pricePaidCredits(packagePurchase.getPricePaidCredits())
                .remainingBalance(remainingBalance)
                .creditTransactionId(packagePurchase.getCreditTransaction().getId())
                .purchasedAt(packagePurchase.getPurchasedAt())
                // No PackageOpening is created in this flow, therefore it is unopened.
                .opened(false)
                .build();
    }
}
