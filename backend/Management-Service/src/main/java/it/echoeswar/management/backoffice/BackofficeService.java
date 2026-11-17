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

@Service
@RequiredArgsConstructor
public class BackofficeService {

    private final CardRepository cardRepository;
    private final FactionRepository factionRepository;
    private final SchoolRepository schoolRepository;
    private final TierRepository tierRepository;

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
                .nameEn(FormatterString.normalizeString(request.nameIt()))
                .descriptionIt(FormatterString.normalizeString(request.descriptionIt()))
                .descriptionEn(FormatterString.normalizeString(request.descriptionEn()))
                .attack(request.attack())
                .defense(request.defense())
                .playCost(request.playCost())
                .imageUrl(FormatterString.normalizeString(request.artUrl()))
                .faction(faction)
                .school(school)
                .tier(tier)
                .build();

        return CardResponse.from(cardRepository.save(card));
    }
}
