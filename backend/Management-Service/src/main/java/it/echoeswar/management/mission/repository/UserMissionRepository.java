package it.echoeswar.management.mission.repository;

import it.echoeswar.management.mission.entity.UserMission;
import it.echoeswar.management.mission.entity.UserMissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMissionRepository extends JpaRepository<UserMission, UserMissionId> {
}
