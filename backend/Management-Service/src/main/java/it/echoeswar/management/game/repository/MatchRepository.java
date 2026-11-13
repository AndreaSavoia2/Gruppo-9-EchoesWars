package it.echoeswar.management.game.repository;

import it.echoeswar.management.game.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
