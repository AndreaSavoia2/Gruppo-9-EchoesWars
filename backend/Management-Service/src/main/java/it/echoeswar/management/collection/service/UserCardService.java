package it.echoeswar.management.collection.service;

import it.echoeswar.management.collection.payload.response.CollectionResponse;
import it.echoeswar.management.collection.repository.UserCardRepository;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.common.utils.BuildPageableResponse;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Application service for reading a card collection for the principal authenticated by the web layer.
 *
 * <p>It obtains the owner ID from {@link CustomUserDetails} instead of accepting a separate user ID.
 * The caller must therefore supply a trusted security principal.</p>
 */
@RequiredArgsConstructor
@Service
public class UserCardService {

    private final UserCardRepository userCardRepository;
    private final UserRepository userRepository;

    /**
     * Retrieves one zero-based page of cards owned by the authenticated user.
     *
     * <p>The read-only transaction stays open while each {@code UserCard} is mapped to a
     * {@link CollectionResponse}, allowing its lazily loaded card, faction, school, and tier to
     * be read. The returned map contains a {@code content} list of collection responses plus
     * {@code totalPages}, {@code totalElements}, and {@code currentPage}.</p>
     *
     * @param customUserDetails trusted authenticated principal whose collection is requested
     * @param pageNumber zero-based page index
     * @param pageSize maximum number of collection entries in the page; must be positive
     * @param sortBy Spring Data property used for sorting; nested properties such as
     *               {@code card.nameIt} are supported
     * @param direction sort direction, either {@code ASC} or {@code DESC}, case-insensitively
     * @return collection page content and pagination metadata
     * @throws ResourceNotFoundException if the principal's user no longer exists
     * @throws IllegalArgumentException if pagination values or {@code direction} are invalid
     * @throws org.springframework.data.core.PropertyReferenceException if {@code sortBy} cannot
     *         be resolved by Spring Data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserCollection(CustomUserDetails customUserDetails,
                                                 int pageNumber,
                                                 int pageSize,
                                                 String sortBy,
                                                 String direction){
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);

        return BuildPageableResponse.buildPage(
                userCardRepository.findAllByUserId(customUserDetails.getId(), pageable)
                        .map(CollectionResponse::from));

    }

}
