package it.echoeswar.management.game.repository;

import it.echoeswar.management.game.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
