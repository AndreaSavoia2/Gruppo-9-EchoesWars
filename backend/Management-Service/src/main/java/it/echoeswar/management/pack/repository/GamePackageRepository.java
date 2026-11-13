package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.GamePackage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePackageRepository extends JpaRepository<GamePackage, Long> {
}
