package it.echoeswar.management.catalog.service;


import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.catalog.repository.CardRepository;
import it.echoeswar.management.common.utils.BuildPageableResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
     * Retrieves one page of cards together with their related faction, school, and tier details.
     *
     * @param pageNumber zero-based page index
     * @param pageSize number of cards to return; must be positive
     * @param sortBy card property used for sorting
     * @param direction sorting direction accepted by {@link Sort.Direction}
     * @return page content together with pagination metadata
     * @throws IllegalArgumentException if page parameters or the sort direction are invalid
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllCards(int pageNumber, int pageSize, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);

        return BuildPageableResponse.buildPage(
                cardRepository.findAll(pageable).map(CardResponse::from));
    }
}
