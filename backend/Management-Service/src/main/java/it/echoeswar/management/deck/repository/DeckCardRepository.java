package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.DeckCard;
import it.echoeswar.management.deck.entity.DeckCardId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckCardRepository extends JpaRepository<DeckCard, DeckCardId> {
}
