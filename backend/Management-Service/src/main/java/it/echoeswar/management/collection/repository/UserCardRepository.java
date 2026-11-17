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
     * Aggiunge copie di una carta alla collezione, creandola se l'utente non la possiede ancora.
     *
     * <p>L'UPSERT viene eseguito direttamente da PostgreSQL per non perdere incrementi nel caso
     * di aperture concorrenti.</p>
     *
     * @param userId proprietario della collezione
     * @param cardId carta da incrementare
     * @param quantity numero positivo di copie da aggiungere
     * @return numero di righe create o aggiornate
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
