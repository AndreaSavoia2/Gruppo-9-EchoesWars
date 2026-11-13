package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.DeckRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRuleRepository extends JpaRepository<DeckRule, Long> {
}
