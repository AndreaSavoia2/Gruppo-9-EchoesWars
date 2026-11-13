package it.echoeswar.management.mission.repository;

import it.echoeswar.management.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}
