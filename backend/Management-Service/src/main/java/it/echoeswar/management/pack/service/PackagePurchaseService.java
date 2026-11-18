package it.echoeswar.management.pack.service;

import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.credit.entity.CreditTransaction;
import it.echoeswar.management.credit.entity.CreditWallet;
import it.echoeswar.management.credit.enumerated.TransactionType;
import it.echoeswar.management.credit.repository.CreditTransactionRepository;
import it.echoeswar.management.credit.repository.CreditWalletRepository;
import it.echoeswar.management.pack.entity.PackagePurchase;
import it.echoeswar.management.pack.entity.PackageVersion;
import it.echoeswar.management.pack.payload.response.PackagePurchaseResponse;
import it.echoeswar.management.pack.repository.PackagePurchaseRepository;
import it.echoeswar.management.pack.repository.PackageVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Coordinates purchases of game packages using a user's credit wallet.
 *
 * <p>A purchase, its negative credit transaction, and the resulting wallet
 * balance change are persisted in one transaction. A pessimistic wallet lock
 * prevents concurrent purchases from overspending the same balance.</p>
 */
@Service
@RequiredArgsConstructor
public class PackagePurchaseService {

    private static final String INSUFFICIENT_CREDITS_MESSAGE =
            "Insufficient credits to purchase this package.";

    private final PackageVersionRepository packageVersionRepository;
    private final CreditWalletRepository creditWalletRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final PackagePurchaseRepository packagePurchaseRepository;

    /**
     * Purchases the most recent active version of a catalog package for a user.
     *
     * <p>The price is always obtained from the persisted package version. No
     * {@code PackageOpening} is created here, so the new purchase remains
     * unopened until the opening flow is explicitly invoked.</p>
     *
     * @param userId identifier of the authenticated buyer
     * @param gamePackageId identifier of the package selected from the catalog
     * @return confirmation of the persisted purchase and remaining credit balance
     * @throws ResourceNotFoundException if no active version is available for the package
     *                                    or the user has no wallet
     * @throws ResourceConflictException if the wallet does not have enough credits
     */
    @Transactional
    public PackagePurchaseResponse purchasePackage(UUID userId, Long gamePackageId) {
        PackageVersion packageVersion = packageVersionRepository
                .findAvailableByGamePackageId(gamePackageId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Available package",
                        "id",
                        gamePackageId
                ));

        CreditWallet wallet = creditWalletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Credit wallet",
                        "user id",
                        userId
                ));

        long price = packageVersion.getPriceCredits();
        if (price <= 0) {
            throw new ResourceConflictException(
                    "The selected package is not available for purchase."
            );
        }
        if (wallet.getBalance() < price) {
            throw new ResourceConflictException(INSUFFICIENT_CREDITS_MESSAGE);
        }

        wallet.setBalance(wallet.getBalance() - price);

        CreditTransaction creditTransaction = creditTransactionRepository.saveAndFlush(
                CreditTransaction.builder()
                        .wallet(wallet)
                        .amount(-price)
                        .transactionType(TransactionType.PACKAGE_PURCHASE)
                        .description(buildPurchaseDescription(packageVersion))
                        .build()
        );

        PackagePurchase packagePurchase = packagePurchaseRepository.saveAndFlush(
                PackagePurchase.builder()
                        .user(wallet.getUser())
                        .packageVersion(packageVersion)
                        .creditTransaction(creditTransaction)
                        .pricePaidCredits(price)
                        .build()
        );

        return PackagePurchaseResponse.from(packagePurchase, wallet.getBalance());
    }

    private String buildPurchaseDescription(PackageVersion packageVersion) {
        return "Purchase of package "
                + packageVersion.getGamePackage().getCode()
                + " version "
                + packageVersion.getVersionNumber()
                + ".";
    }
}
