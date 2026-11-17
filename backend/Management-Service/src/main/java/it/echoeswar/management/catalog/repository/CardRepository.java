package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.Card;
import it.echoeswar.management.catalog.enumerated.TierCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Override
    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"faction", "school", "tier"}
    )
    Page<Card> findAll(Pageable pageable);

    /**
     * Restituisce le carte attive di una rarità con tutte le relazioni necessarie alla risposta API.
     *
     * @param tierCode rarità da cui estrarre le carte
     * @return carte disponibili per l'estrazione, ordinate in modo stabile
     */
    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"faction", "school", "tier"}
    )
    @Query("""
            select card
            from Card card
            join card.tier tier
            where card.active = true
              and tier.active = true
              and tier.code = :tierCode
            order by card.id
            """)
    List<Card> findActiveByTierCode(@Param("tierCode") TierCode tierCode);
}
