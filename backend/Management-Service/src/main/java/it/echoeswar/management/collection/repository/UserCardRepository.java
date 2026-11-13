package it.echoeswar.management.collection.repository;

import it.echoeswar.management.collection.entity.UserCard;
import it.echoeswar.management.collection.entity.UserCardId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCardRepository extends JpaRepository<UserCard, UserCardId> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"card","card.faction", "card.school", "card.tier"}
    )
    Page<UserCard> findAllByUserId(UUID userId, Pageable pageable);
}
