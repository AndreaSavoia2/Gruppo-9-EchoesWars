package it.echoeswar.management.game.repository;

import it.echoeswar.management.game.entity.SimulationJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationJobRepository extends JpaRepository<SimulationJob, Long> {
}
