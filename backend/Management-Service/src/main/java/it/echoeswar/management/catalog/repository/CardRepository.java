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

    boolean existsByCodeIgnoreCase(String code);

    /**
     * Returns active cards of a given rarity with every relation needed by the API response.
     *
     * @param tierCode rarity from which cards are drawn
     * @return cards available for drawing, in stable order
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
