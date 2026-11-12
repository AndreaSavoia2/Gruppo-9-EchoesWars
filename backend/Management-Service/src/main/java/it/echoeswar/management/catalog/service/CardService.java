package it.echoeswar.management.catalog.service;


import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.catalog.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service providing read operations on cards.
 *
 * <p>All methods run in read-only transactions and return {@link CardResponse}
 * objects instead of exposing the {@link Card} entity directly.</p>
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    /**
     * Retrieves a single card by its identifier.
     *
     * @param id the identifier of the card to retrieve
     * @return the card mapped to a {@link CardResponse}
     * @throws EntityNotFoundException if no card exists with the given id
     */
    @Transactional(readOnly = true)
    public CardResponse getCard(long id){
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id: " + id + " not found"));
        return CardResponse.from(card);
    }

    /**
     * Retrieves all cards together with their related faction, school and tiar details.
     *
     * @return the list of all cards mapped to {@link CardResponse}; empty if there are none
     */
    @Transactional(readOnly = true)
    public List<CardResponse> getCardDetails(){
        return cardRepository.findAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }
}
