package it.echoeswar.management.backoffice;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.entity.Faction;
import it.echoeswar.management.catalog.entity.School;
import it.echoeswar.management.catalog.entity.Tier;
import it.echoeswar.management.catalog.payload.request.CardRequest;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.catalog.repository.CardRepository;
import it.echoeswar.management.catalog.repository.FactionRepository;
import it.echoeswar.management.catalog.repository.SchoolRepository;
import it.echoeswar.management.catalog.repository.TierRepository;
import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.common.utils.FormatterString;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Creates a new card in the catalog.
     *
     * <p>The method first verifies that no card with the same code exists
     * (case-insensitive), then resolves the faction, school and tier
     * referenced by the request, and finally builds and persists the card.
     * String fields are normalized through {@link FormatterString#normalizeString(String)}
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
                .code(FormatterString.normalizeString(request.code()))
                .nameIt(FormatterString.normalizeString(request.nameIt()))
                .nameEn(FormatterString.normalizeString(request.nameEn()))
                .descriptionIt(FormatterString.normalizeString(request.descriptionIt()))
                .descriptionEn(FormatterString.normalizeString(request.descriptionEn()))
                .attack(request.attack())
                .defense(request.defense())
                .playCost(request.playCost())
                .maxCopyDeck(request.maxCopyDeck())
                .imageUrl(FormatterString.normalizeString(request.artUrl()))
                .faction(faction)
                .school(school)
                .tier(tier)
                .build();

        return CardResponse.from(cardRepository.save(card));
    }
}