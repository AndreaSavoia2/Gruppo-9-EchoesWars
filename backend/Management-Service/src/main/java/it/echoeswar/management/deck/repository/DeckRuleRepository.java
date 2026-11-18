package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.DeckRule;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface DeckRuleRepository extends JpaRepository<DeckRule, Long> {

    Optional<DeckRule> findDeckRuleByActiveTrue();
}
