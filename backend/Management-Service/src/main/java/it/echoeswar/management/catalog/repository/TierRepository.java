package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierRepository extends JpaRepository<Tier, Byte> {
}
