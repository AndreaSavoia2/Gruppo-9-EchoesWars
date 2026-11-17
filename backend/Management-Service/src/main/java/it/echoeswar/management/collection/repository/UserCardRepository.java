package it.echoeswar.management.collection.repository;

import it.echoeswar.management.collection.entity.UserCard;
import it.echoeswar.management.collection.entity.UserCardId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserCardRepository extends JpaRepository<UserCard, UserCardId> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"card","card.faction", "card.school", "card.tier"}
    )
    Page<UserCard> findAllByUserId(UUID userId, Pageable pageable);

    /**
     * Adds copies of a card to the collection, creating the record if the user does not yet own it.
     *
     * <p>The UPSERT runs directly in PostgreSQL to avoid losing increments when openings happen
     * concurrently.</p>
     *
     * @param userId collection owner
     * @param cardId card to increment
     * @param quantity positive number of copies to add
     * @return number of rows created or updated
     */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into user_cards (user_id, card_id, quantity, created_at, updated_at)
            values (:userId, :cardId, :quantity, localtimestamp(6), localtimestamp(6))
            on conflict (user_id, card_id)
            do update set quantity = user_cards.quantity + excluded.quantity,
                          updated_at = localtimestamp(6)
            """, nativeQuery = true)
    int incrementQuantity(
            @Param("userId") UUID userId,
            @Param("cardId") long cardId,
            @Param("quantity") int quantity
    );
}
