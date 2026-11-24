package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.GamePackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamePackageRepository extends JpaRepository<GamePackage, Long> {
    boolean existsByCodeIgnoreCase(String code);

    List<GamePackage> findAllByActiveTrueOrderByIdAsc();
}
