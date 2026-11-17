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
import it.echoeswar.management.pack.entity.PackagePurchase;
import it.echoeswar.management.pack.entity.PackageVersion;
import it.echoeswar.management.pack.payload.response.PackageOpeningResponse;
import it.echoeswar.management.pack.payload.response.PackagePurchaseResponse;
import it.echoeswar.management.pack.payload.response.UnopenedPackagePurchaseResponse;
import it.echoeswar.management.pack.repository.PackageOpeningRepository;
import it.echoeswar.management.pack.repository.PackagePurchaseRepository;
import it.echoeswar.management.pack.repository.PackageVersionRepository;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumMap;
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
    private static final int CARDS_PER_PACKAGE = 5;
    private static final int GUARANTEED_COMMON_CARDS = 3;
    private static final int GUARANTEED_RARE_CARDS = 1;
    private static final int CYCLE_LENGTH = 10;
    private static final int RANDOM_COMMON_THRESHOLD = 85;
    private static final int RANDOM_RARE_THRESHOLD = 98;
    private static final SecureRandom SEED_GENERATOR = new SecureRandom();

    private final PackageVersionRepository packageVersionRepository;
    private final CreditWalletRepository creditWalletRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final PackagePurchaseRepository packagePurchaseRepository;
    private final PackageOpeningRepository packageOpeningRepository;
    private final CardRepository cardRepository;
    private final UserCardService userCardService;
    private final UserRepository userRepository;

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
     * Apre un pacchetto acquistato e aggiunge immediatamente le carte alla collezione dell'utente.
     *
     * <p>Le prime nove aperture del ciclo ricevono tre comuni, una rara e uno slot casuale
     * (85% comune, 13% rara, 2% leggendaria). La decima sostituisce lo slot casuale con una
     * leggendaria garantita; il conteggio poi riparte automaticamente dal primo slot del ciclo.</p>
     *
     * @param userId identificativo dell'utente autenticato
     * @param purchaseId identificativo dell'acquisto da aprire
     * @return apertura persistita con le cinque carte estratte
     * @throws ResourceNotFoundException se l'utente o l'acquisto non esistono
     * @throws ResourceConflictException se l'acquisto è già aperto o manca una rarità attiva
     */
    @Transactional
    public PackageOpeningResponse openPackage(UUID userId, Long purchaseId) {
        // 1. Il lock dell'utente serializza le aperture dell'account e rende certo il decimo pacchetto.
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 2. Il lock dell'acquisto impedisce che lo stesso pacchetto venga aperto in parallelo due volte.
        PackagePurchase packagePurchase = packagePurchaseRepository
                .findByIdAndUserIdForUpdate(purchaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Package purchase",
                        "id",
                        purchaseId
                ));

        if (packageOpeningRepository.existsByPackagePurchaseId(purchaseId)) {
            throw new ResourceConflictException("This package has already been opened.");
        }

        // 3. Il contatore considera solo le aperture concluse: le leggendarie casuali non lo alterano.
        long openingNumber = packageOpeningRepository.countByUserId(userId) + 1;
        short cyclePosition = (short) (((openingNumber - 1) % CYCLE_LENGTH) + 1);
        boolean legendaryGuaranteed = cyclePosition == CYCLE_LENGTH;

        // 4. Carichiamo una volta le tre pool attive; ogni indice estratto è uniforme nella sua rarità.
        Map<TierCode, List<Card>> cardsByTier = loadActiveCardsByTier();

        // 5. Un seed sicuro viene salvato per tracciare l'estrazione senza riusare lo stesso flusso casuale.
        long randomSeed = SEED_GENERATOR.nextLong();
        Random random = new Random(randomSeed);
        List<Card> drawnCards = new ArrayList<>(CARDS_PER_PACKAGE);

        for (int index = 0; index < GUARANTEED_COMMON_CARDS; index++) {
            drawnCards.add(drawCard(TierCode.COMMON, cardsByTier, random));
        }
        for (int index = 0; index < GUARANTEED_RARE_CARDS; index++) {
            drawnCards.add(drawCard(TierCode.RARE, cardsByTier, random));
        }

        TierCode fifthSlotTier = legendaryGuaranteed
                ? TierCode.LEGENDARY
                : resolveRandomSlotTier(random);
        drawnCards.add(drawCard(fifthSlotTier, cardsByTier, random));

        // 6. I duplicati restano nella risposta, ma vengono sommati prima di aggiornare la collezione.
        Map<Long, Integer> copiesByCardId = new LinkedHashMap<>();
        for (Card card : drawnCards) {
            copiesByCardId.merge(card.getId(), 1, Integer::sum);
        }
        userCardService.addCardsToCollection(userId, copiesByCardId);

        // 7. Salviamo l'apertura solo dopo l'accredito: l'intera operazione fa rollback in caso di errore.
        PackageOpening packageOpening = packageOpeningRepository.saveAndFlush(
                PackageOpening.builder()
                        .packagePurchase(packagePurchase)
                        .randomSeed(Long.toUnsignedString(randomSeed))
                        .openingNumber(openingNumber)
                        .cyclePosition(cyclePosition)
                        .wasGuaranteed(legendaryGuaranteed)
                        .build()
        );

        // 8. La risposta mantiene esattamente cinque slot e conserva anche eventuali carte duplicate.
        return PackageOpeningResponse.from(packageOpening, drawnCards);
    }

    private Map<TierCode, List<Card>> loadActiveCardsByTier() {
        Map<TierCode, List<Card>> cardsByTier = new EnumMap<>(TierCode.class);

        for (TierCode tierCode : TierCode.values()) {
            List<Card> cards = cardRepository.findActiveByTierCode(tierCode);
            if (cards.isEmpty()) {
                throw new ResourceConflictException(
                        "No active cards are available for rarity: " + tierCode + "."
                );
            }
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

    private TierCode resolveRandomSlotTier(Random random) {
        int roll = random.nextInt(100);
        if (roll < RANDOM_COMMON_THRESHOLD) {
            return TierCode.COMMON;
        }
        if (roll < RANDOM_RARE_THRESHOLD) {
            return TierCode.RARE;
        }
        return TierCode.LEGENDARY;
    }

    private String buildPurchaseDescription(PackageVersion packageVersion) {
        return "Purchase of package "
                + packageVersion.getGamePackage().getCode()
                + " version "
                + packageVersion.getVersionNumber()
                + ".";
    }
}
