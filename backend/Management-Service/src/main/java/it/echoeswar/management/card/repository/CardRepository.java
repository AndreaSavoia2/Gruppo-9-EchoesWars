package it.echoeswar.management.card.repository;

import it.echoeswar.management.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
