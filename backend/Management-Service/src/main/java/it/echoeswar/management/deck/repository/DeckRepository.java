package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<Deck, Long> {
}
