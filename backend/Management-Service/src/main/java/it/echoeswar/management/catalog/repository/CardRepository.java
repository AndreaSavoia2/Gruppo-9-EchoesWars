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

    /**
     * Finds the highest sequential number among existing card codes with the given prefix.
     * <p>
     * Only codes matching the {@code <prefix><number>} pattern (case-insensitive) are considered,
     * for example {@code DOM01} or {@code DOM12}. The match uses a PostgreSQL regular expression
     * ({@code ^PREFIX[0-9]+$}), so codes in any other format (such as {@code DOM_01}) are ignored.
     * The numeric part is extracted by skipping the first three characters, which are the prefix.
     *
     * @param prefix the card code prefix, in uppercase (e.g. {@code DOM})
     * @return the highest number found, or {@code 0} if no matching code exists
     */
    @Query(value = """
        select coalesce(max(cast(substring(code from 4) as integer)), 0)
        from cards
        where upper(code) ~ concat('^', :prefix, '[0-9]+$')
        """, nativeQuery = true)
    int findMaxNumberByPrefix(@Param("prefix") String prefix);
}
