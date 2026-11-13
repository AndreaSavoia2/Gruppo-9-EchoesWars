package it.echoeswar.management.game.repository;

import it.echoeswar.management.game.entity.GameRuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRuleVersionRepository extends JpaRepository<GameRuleVersion, Long> {
}
