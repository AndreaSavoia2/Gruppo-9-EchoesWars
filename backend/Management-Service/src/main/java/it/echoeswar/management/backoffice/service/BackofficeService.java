package it.echoeswar.management.backoffice.service;

import it.echoeswar.management.backoffice.payload.request.PackRequest;
import it.echoeswar.management.backoffice.payload.request.PackUpdateVersionRequest;
import it.echoeswar.management.backoffice.payload.response.FactionResponse;
import it.echoeswar.management.backoffice.payload.response.PackResponse;
import it.echoeswar.management.backoffice.payload.response.SchoolResponse;
import it.echoeswar.management.backoffice.payload.response.TierResponse;
import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.catalog.entity.School;
import it.echoeswar.management.catalog.entity.Tier;
import it.echoeswar.management.backoffice.payload.request.CardRequest;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.catalog.repository.CardRepository;
import it.echoeswar.management.catalog.repository.FactionRepository;
import it.echoeswar.management.catalog.repository.SchoolRepository;
import it.echoeswar.management.catalog.repository.TierRepository;
import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.pack.entity.GamePackage;
import it.echoeswar.management.pack.entity.PackagePullRule;
import it.echoeswar.management.pack.entity.PackageVersion;
import it.echoeswar.management.pack.repository.GamePackageRepository;
import it.echoeswar.management.pack.repository.PackagePullRuleRepository;
import it.echoeswar.management.pack.repository.PackageVersionRepository;
import it.echoeswar.management.pack.utils.ValidatePackUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service containing the back-office business logic used by administrators
 * to manage the game catalog (cards, factions, schools and tiers).
 */
@Service
@RequiredArgsConstructor
public class BackofficeService {

    /** Repository used to persist and query cards. */
    private final CardRepository cardRepository;

    /** Repository used to look up the factions a card can belong to. */
    private final FactionRepository factionRepository;

    /** Repository used to look up the schools a card can belong to. */
    private final SchoolRepository schoolRepository;

    /** Repository used to look up the tiers (rarity levels) of a card. */
    private final TierRepository tierRepository;
    private final GamePackageRepository gamePackageRepository;

    /** Method use to validate the rule of packages creates*/
    private final ValidatePackUtils validatePackUtils;

    private final PackagePullRuleRepository packagePullRuleRepository;
    private final PackageVersionRepository packageVersionRepository;

    /**
     * Creates a new card in the catalog.
     *
     * <p>The method first verifies that no card with the same code exists
     * (case-insensitive), then resolves the faction, school and tier
     * referenced by the request, and finally builds and persists the card.
     * String fields are normalized through {@link FormatterString#normalizeLowerCase(String)} (String)
     * before being saved.</p>
     *
     * @param request the payload containing the card data and the identifiers
     *                of the related faction, school and tier
     * @return the {@link CardResponse} representing the newly created card
     * @throws ResourceConflictException if a card with the same code already exists
     * @throws ResourceNotFoundException if the faction, school or tier
     *                                   referenced by the request does not exist
     */
    @Transactional
    public CardResponse createCard(CardRequest request){
        if(cardRepository.existsByCodeIgnoreCase(request.code())){
            throw new ResourceConflictException("Card code already exists: " + request.code());
        }

        Faction faction = factionRepository.findById(request.echoesId())
                .orElseThrow(() -> new ResourceNotFoundException("Faction", "id", request.echoesId()));
        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", request.schoolId()));
        Tier tier = tierRepository.findById(request.tierId())
                .orElseThrow(() -> new ResourceNotFoundException("Tier", "id", request.tierId()));

        Card card = Card.builder()
                .code(FormatterString.normalizeLowerCase(request.code()))
                .nameIt(FormatterString.normalizeLowerCase(request.nameIt()))
                .nameEn(FormatterString.normalizeLowerCase(request.nameEn()))
                .descriptionIt(FormatterString.normalizeLowerCase(request.descriptionIt()))
                .descriptionEn(FormatterString.normalizeLowerCase(request.descriptionEn()))
                .attack(request.attack())
                .defense(request.defense())
                .playCost(request.playCost())
                .maxCopyDeck(request.maxCopyDeck())
                .imageUrl(FormatterString.normalizeLowerCase(request.artUrl()))
                .faction(faction)
                .school(school)
                .tier(tier)
                .build();

        return CardResponse.from(cardRepository.save(card));
    }

    /**
     * Creates a new pack in the catalog.
     *
     * <p>The method first verifies that no pack with the same code exists
     * (case-insensitive), then builds a {@link GamePackage} from the request
     * and persists it. String fields are normalized through
     * {@link FormatterString#normalizeLowerCase(String)} (String) before being saved.</p>
     *
     * @param request the payload containing the pack code, names and
     *                optional descriptions
     * @return the {@link PackResponse} representing the newly created pack
     * @throws ResourceConflictException if a pack with the same code already exists
     */
    @Transactional
    public PackResponse createPack(PackRequest request){

//        if(gamePackageRepository.existsByCodeIgnoreCase(request.code())){
//            throw new ResourceConflictException("Pack code already exists: " + request.code());
//        }



        GamePackage gamePackage = GamePackage.builder()
                .code(FormatterString.normalizeLowerCase(generateCode()))
                .nameIt(FormatterString.normalizeLowerCase(request.nameIt()))
                .nameEn(FormatterString.normalizeLowerCase(request.nameEn()))
                .descriptionIt(FormatterString.normalizeLowerCase(request.descriptionIt()))
                .descriptionEn(FormatterString.normalizeLowerCase(request.descriptionEn()))
                .build();



        PackageVersion packageVersion = PackageVersion.builder()
                .gamePackage(gamePackage)
                .versionNumber(1)
                .priceCredits(request.priceCredits())
                .publishedAt(LocalDateTime.now())
                .build();

        PackagePullRule packagePullRule = PackagePullRule.builder()
                .packageVersion(packageVersion)
                .cardsPerPackage(request.cardsPerPackage())
                .guaranteedCommonCards(request.guaranteCommonCards())
                .guaranteedRareCards(request.guaranteRareCards())
                .cycleLength(request.cycleLength())
                .randomCommonThreshold(request.randomCommonThreshold())
                .randomRareThreshold(request.randomRareThreshold())
                .build();

        validatePackUtils.validatePullRule(packagePullRule);

        gamePackageRepository.save(gamePackage);
        packageVersionRepository.save(packageVersion);
        packagePullRuleRepository.save(packagePullRule);

        return PackResponse.from(gamePackageRepository.save(gamePackage));

    }

    //Modificare il pacchetto con delle nuove regole
    @Transactional
    public String updatePullRulePack(PackUpdateVersionRequest request){

        PackageVersion packageVersion = packageVersionRepository.findVersionByPackageId(request.packageId())
                .orElseThrow(()-> new ResourceNotFoundException("packageId", "id", request.packageId()));

        PackageVersion newPackageVersion = PackageVersion.builder()
                .gamePackage(packageVersion.getGamePackage())
                .versionNumber(packageVersion.getVersionNumber()+1)
                .priceCredits(request.priceCredits())
                .publishedAt(LocalDateTime.now())
                .build();

        newPackageVersion = packageVersionRepository.save(newPackageVersion);   // <-- AGGIUNGI QUESTA RIGA


        PackagePullRule newPackagePullRule = PackagePullRule.builder()
                .packageVersion(newPackageVersion)
                .cardsPerPackage(request.cardsPerPackage())
                .guaranteedCommonCards(request.guaranteCommonCards())
                .guaranteedRareCards(request.guaranteRareCards())
                .cycleLength(request.cycleLength())
                .randomCommonThreshold(request.randomCommonThreshold())
                .randomRareThreshold(request.randomRareThreshold())
                .build();

        validatePackUtils.validatePullRule(newPackagePullRule);
        packagePullRuleRepository.save(newPackagePullRule);

        return "complete";
    }

    /**
     * Retrieves all tiers.
     *
     * @return the list of all tiers as {@link TierResponse} objects, or an empty list if none exist
     */
    @Transactional(readOnly = true)
    public List<TierResponse> getAllTiers() {
        return tierRepository.findAll().stream()
                .map(TierResponse::from)
                .toList();
    }

    /**
     * Retrieves all factions.
     *
     * @return the list of all factions as {@link FactionResponse} objects, or an empty list if none exist
     */
    @Transactional(readOnly = true)
    public List<FactionResponse> getAllFactions() {
        return factionRepository.findAll().stream()
                .map(FactionResponse::from)
                .toList();
    }

    /**
     * Retrieves all schools.
     *
     * @return the list of all schools as {@link SchoolResponse} objects, or an empty list if none exist
     */
    @Transactional(readOnly = true)
    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(SchoolResponse::from)
                .toList();
    }

    /**
     * Prefix used to build game package codes (e.g. {@code PACK_01}).
     */
    private static final String CODE_PREFIX = "PACK";

    /**
     * Generates the next sequential game package code.
     * <p>
     * The code is built from {@link #CODE_PREFIX} and the highest existing number plus one,
     * zero-padded to at least two digits (e.g. {@code PACK_01}, ..., {@code PACK_99}, {@code PACK_100}).
     *
     * @return the next available game package code
     */
    private String generateCode() {
        int next = gamePackageRepository.findMaxNumber() + 1;
        return String.format("%s_%02d", CODE_PREFIX, next);   // PACK_01, ..., PACK_99, PACK_100
    }

}