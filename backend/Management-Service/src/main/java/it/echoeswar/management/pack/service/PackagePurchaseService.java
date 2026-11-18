package it.echoeswar.management.pack.service;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.enumerated.TierCode;
import it.echoeswar.management.catalog.repository.CardRepository;
import it.echoeswar.management.collection.service.UserCardService;
import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.credit.entity.CreditTransaction;
import it.echoeswar.management.credit.entity.CreditWallet;
import it.echoeswar.management.credit.enumerated.TransactionType;
import it.echoeswar.management.credit.repository.CreditTransactionRepository;
import it.echoeswar.management.credit.repository.CreditWalletRepository;
import it.echoeswar.management.pack.entity.PackageOpening;
import it.echoeswar.management.pack.entity.PackagePullRule;
import it.echoeswar.management.pack.entity.PackagePurchase;
import it.echoeswar.management.pack.entity.PackageVersion;
import it.echoeswar.management.pack.payload.response.PackageOpeningResponse;
import it.echoeswar.management.pack.payload.response.PackageLegendaryProgressResponse;
import it.echoeswar.management.pack.payload.response.PackagePurchaseResponse;
import it.echoeswar.management.pack.payload.response.UnopenedPackagePurchaseResponse;
import it.echoeswar.management.pack.repository.PackageOpeningRepository;
import it.echoeswar.management.pack.repository.PackagePullRuleRepository;
import it.echoeswar.management.pack.repository.PackagePurchaseRepository;
import it.echoeswar.management.pack.repository.PackageVersionRepository;
import it.echoeswar.management.pack.utils.ValidatePackUtils;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private static final SecureRandom SEED_GENERATOR = new SecureRandom();

    private final PackageVersionRepository packageVersionRepository;
    private final PackagePullRuleRepository packagePullRuleRepository;
    private final CreditWalletRepository creditWalletRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final PackagePurchaseRepository packagePurchaseRepository;
    private final PackageOpeningRepository packageOpeningRepository;
    private final CardRepository cardRepository;
    private final UserCardService userCardService;
    private final UserRepository userRepository;
    private final ValidatePackUtils validatePackUtils;

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
     * @throws ResourceConflictException if the wallet does not have enough credits or the
     *                                   selected version has no valid pull rule
     */
    @Transactional
    public PackagePurchaseResponse purchasePackage(UUID userId, Long gamePackageId) {
        PackageVersion packageVersion = packageVersionRepository
                .findAvailableByGamePackageId(gamePackageId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Available package", "id", gamePackageId));

        // The sale is allowed only when the version has a complete, immutable pull rule.
        validatePackUtils.validatePullRule(findPullRuleForVersion(packageVersion));

        CreditWallet wallet = creditWalletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit wallet", "user id", userId));

        long price = packageVersion.getPriceCredits();
        if (price <= 0)
            throw new ResourceConflictException("The selected package is not available for purchase.");
        if (wallet.getBalance() < price)
            throw new ResourceConflictException(INSUFFICIENT_CREDITS_MESSAGE);

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
                        .packageVersion(packageVersion)
                        .creditTransaction(creditTransaction)
                        .pricePaidCredits(price)
                        .build()
        );

        return PackagePurchaseResponse.from(packagePurchase, wallet.getBalance());
    }

    /**
     * Retrieves every package purchase owned by a user that has not yet been opened.
     *
     * @param userId identifier of the authenticated user
     * @return unopened package purchases, ordered from newest to oldest
     */
    @Transactional(readOnly = true)
    public List<UnopenedPackagePurchaseResponse> getUnopenedPurchases(UUID userId) {
        return packagePurchaseRepository.findUnopenedByUserId(userId)
                .stream()
                .map(UnopenedPackagePurchaseResponse::from)
                .toList();
    }

    /**
     * Calculates how many account openings remain until the next guaranteed legendary
     * under the historical rule of an unopened purchase.
     *
     * <p>The counter is global to the account, as in the actual opening flow. The cycle,
     * however, is read from the purchased package version: future catalog configuration
     * changes therefore do not affect this calculation.</p>
     *
     * @param userId identifier of the authenticated user
     * @param purchaseId identifier of the unopened purchase to inspect
     * @return progress toward the next opening with a guaranteed legendary
     * @throws ResourceNotFoundException if the purchase is already opened or does not belong to the user
     * @throws ResourceConflictException if the purchased version has no valid pull rule
     */
    @Transactional(readOnly = true)
    public PackageLegendaryProgressResponse getLegendaryGuaranteeProgress(UUID userId, Long purchaseId) {
        // The read is limited to the unopened purchase owned by the authenticated user.
        PackagePurchase packagePurchase = packagePurchaseRepository
                .findUnopenedByIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Unopened package purchase", "id", purchaseId));

        // Retrieve the immutable rule of the version that was actually purchased.
        PackagePullRule pullRule = findPullRuleForVersion(packagePurchase.getPackageVersion());
        validatePackUtils.validatePullRule(pullRule);

        // Apply the same global progression used by openPackage.
        long completedOpenings = packageOpeningRepository.countByUserId(userId);
        int completedInCurrentCycle = (int) (completedOpenings % pullRule.getCycleLength());
        int openingsUntilGuaranteedLegendary = pullRule.getCycleLength() - completedInCurrentCycle;
        long guaranteedOpeningNumber = completedOpenings + openingsUntilGuaranteedLegendary;

        return PackageLegendaryProgressResponse.builder()
                .packagePurchaseId(packagePurchase.getId())
                .packageVersionId(packagePurchase.getPackageVersion().getId())
                .packageId(packagePurchase.getPackageVersion().getGamePackage().getId())
                .cycleLength(pullRule.getCycleLength())
                .completedOpenings(completedOpenings)
                .completedOpeningsInCurrentCycle(completedInCurrentCycle)
                .openingsUntilGuaranteedLegendary(openingsUntilGuaranteedLegendary)
                .guaranteedOpeningNumber(guaranteedOpeningNumber)
                .legendaryGuaranteedIfOpenedNext(openingsUntilGuaranteedLegendary == 1)
                .build();
    }

    /**
     * Opens a purchased package and immediately adds the drawn cards to the user's collection.
     *
     * <p>The quantities, cycle, and probabilities come from the immutable {@link PackagePullRule}
     * of the {@link PackageVersion} stored on the purchase. New package versions therefore do not
     * alter the outcome of packages that were already purchased.</p>
     *
     * @param userId identifier of the authenticated user
     * @param purchaseId identifier of the purchase to open
     * @return persisted opening with the drawn cards
     * @throws ResourceNotFoundException if the user or purchase does not exist
     * @throws ResourceConflictException if the purchase is already open, the rule is invalid,
     *                                   or a required active rarity is missing
     */
    @Transactional
    public PackageOpeningResponse openPackage(UUID userId, Long purchaseId) {
        // The user lock serializes account openings and makes the cycle calculation reliable.
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // The purchase lock prevents the same package from being opened twice in parallel.
        PackagePurchase packagePurchase = packagePurchaseRepository
                .findByIdAndUserIdForUpdate(purchaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Package purchase", "id", purchaseId));

        if (packageOpeningRepository.existsByPackagePurchaseId(purchaseId))
            throw new ResourceConflictException("This package has already been opened.");

        // Only the rule of the purchased version is used, not the version currently active.
        PackagePullRule pullRule = findPullRuleForVersion(packagePurchase.getPackageVersion());
        validatePackUtils.validatePullRule(pullRule);

        // Only completed openings are counted; randomly drawn legendaries do not affect the counter.
        long openingNumber = packageOpeningRepository.countByUserId(userId) + 1;
        int cyclePosition = (int) (((openingNumber - 1) % pullRule.getCycleLength()) + 1);
        boolean legendaryGuaranteed = cyclePosition == pullRule.getCycleLength();

        // Only required rarities are loaded; each draw is uniform within its rarity.
        Map<TierCode, List<Card>> cardsByTier = loadActiveCardsByTier(pullRule, legendaryGuaranteed);

        // A secure seed is stored to trace the draw without reusing the same random sequence.
        long randomSeed = SEED_GENERATOR.nextLong();
        Random random = new Random(randomSeed);
        List<Card> drawnCards = drawCards(pullRule, cardsByTier, legendaryGuaranteed, random);

        // Duplicates remain in the response but are aggregated before updating the collection.
        Map<Long, Integer> copiesByCardId = new LinkedHashMap<>();
        for (Card card : drawnCards)
            copiesByCardId.merge(card.getId(), 1, Integer::sum);
        userCardService.addCardsToCollection(userId, copiesByCardId);

        // Save the opening only after crediting the collection; the whole operation rolls back on error.
        PackageOpening packageOpening = packageOpeningRepository.saveAndFlush(
                PackageOpening.builder()
                        .packagePurchase(packagePurchase)
                        .randomSeed(Long.toUnsignedString(randomSeed))
                        .openingNumber(openingNumber)
                        .cyclePosition(cyclePosition)
                        .wasGuaranteed(legendaryGuaranteed)
                        .build()
        );

        // The response preserves slot order and any duplicate cards.
        return PackageOpeningResponse.from(packageOpening, drawnCards);
    }

    /**
     * Draws guaranteed cards and all remaining slots defined by the historical rule.
     * Remaining slots are random during ordinary openings and legendary at the end of the cycle.
     */
    private List<Card> drawCards(
            PackagePullRule pullRule,
            Map<TierCode, List<Card>> cardsByTier,
            boolean legendaryGuaranteed,
            Random random) {
        List<Card> drawnCards = new ArrayList<>(pullRule.getCardsPerPackage());

        for (int index = 0; index < pullRule.getGuaranteedCommonCards(); index++)
            drawnCards.add(drawCard(TierCode.COMMON, cardsByTier, random));

        for (int index = 0; index < pullRule.getGuaranteedRareCards(); index++)
            drawnCards.add(drawCard(TierCode.RARE, cardsByTier, random));


        int variableSlots = pullRule.getCardsPerPackage()
                - pullRule.getGuaranteedCommonCards()
                - pullRule.getGuaranteedRareCards();
        for (int index = 0; index < variableSlots; index++) {
            TierCode tierCode = legendaryGuaranteed
                    ? TierCode.LEGENDARY
                    : resolveRandomSlotTier(pullRule, random);
            drawnCards.add(drawCard(tierCode, cardsByTier, random));
        }

        return drawnCards;
    }

    /**
     * Loads only the card pools required by the configuration applied to this opening.
     */
    private Map<TierCode, List<Card>> loadActiveCardsByTier(
            PackagePullRule pullRule,
            boolean legendaryGuaranteed
    ) {
        Map<TierCode, List<Card>> cardsByTier = new EnumMap<>(TierCode.class);
        EnumSet<TierCode> requiredTiers = EnumSet.noneOf(TierCode.class);

        if (pullRule.getGuaranteedCommonCards() > 0 ||
                (!legendaryGuaranteed && pullRule.getRandomCommonThreshold() > 0))
            requiredTiers.add(TierCode.COMMON);

        if (pullRule.getGuaranteedRareCards() > 0 ||
                (!legendaryGuaranteed && pullRule.getRandomRareThreshold() > pullRule.getRandomCommonThreshold())) {
            requiredTiers.add(TierCode.RARE);
        }
        if (legendaryGuaranteed || (!legendaryGuaranteed && pullRule.getRandomRareThreshold() < 100))
            requiredTiers.add(TierCode.LEGENDARY);


        for (TierCode tierCode : requiredTiers) {
            List<Card> cards = cardRepository.findActiveByTierCode(tierCode);
            if (cards.isEmpty())
                throw new ResourceConflictException("No active cards are available for rarity: " + tierCode + ".");

            cardsByTier.put(tierCode, cards);
        }

        return cardsByTier;
    }

    private Card drawCard(
            TierCode tierCode,
            Map<TierCode, List<Card>> cardsByTier,
            Random random
    ) {
        List<Card> cards = cardsByTier.get(tierCode);
        return cards.get(random.nextInt(cards.size()));
    }

    /**
     * Applies the cumulative thresholds stored in the rule: common, rare, or legendary.
     */
    private TierCode resolveRandomSlotTier(PackagePullRule pullRule, Random random) {
        int roll = random.nextInt(100);
        if (roll < pullRule.getRandomCommonThreshold()) {
            return TierCode.COMMON;
        }
        if (roll < pullRule.getRandomRareThreshold()) {
            return TierCode.RARE;
        }
        return TierCode.LEGENDARY;
    }

    /**
     * Retrieves only the rule associated with the version that was actually purchased.
     */
    private PackagePullRule findPullRuleForVersion(PackageVersion packageVersion) {
        return packagePullRuleRepository.findByPackageVersionId(packageVersion.getId())
                .orElseThrow(() -> new ResourceConflictException(
                        "The selected package version has no pull rule."
                ));
    }

    private String buildPurchaseDescription(PackageVersion packageVersion) {
        return "Purchase of package "
                + packageVersion.getGamePackage().getCode()
                + " version "
                + packageVersion.getVersionNumber()
                + ".";
    }
}
