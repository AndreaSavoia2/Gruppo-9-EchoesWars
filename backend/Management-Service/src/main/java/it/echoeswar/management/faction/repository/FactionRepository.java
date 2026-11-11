package it.echoeswar.management.faction.repository;

import it.echoeswar.management.faction.entity.Faction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactionRepository extends JpaRepository<Faction, Byte> {
}
