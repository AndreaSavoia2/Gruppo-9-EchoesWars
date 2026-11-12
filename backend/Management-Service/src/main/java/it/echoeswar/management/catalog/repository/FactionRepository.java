package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.Faction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactionRepository extends JpaRepository<Faction, Byte> {
}
