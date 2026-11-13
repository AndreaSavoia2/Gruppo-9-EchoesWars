package it.echoeswar.management.deck.repository;

import it.echoeswar.management.deck.entity.DeckFaction;
import it.echoeswar.management.deck.entity.DeckFactionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckFactionRepository extends JpaRepository<DeckFaction, DeckFactionId> {
}
