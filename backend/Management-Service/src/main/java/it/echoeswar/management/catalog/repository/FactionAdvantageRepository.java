package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.FactionAdvantage;
import it.echoeswar.management.catalog.entity.FactionAdvantageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactionAdvantageRepository extends JpaRepository<FactionAdvantage, FactionAdvantageId> {
}
