package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Override
    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"faction", "school", "tier"}
    )
    Page<Card> findAll(Pageable pageable);
}
