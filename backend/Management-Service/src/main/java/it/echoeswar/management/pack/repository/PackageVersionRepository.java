package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackageVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageVersionRepository extends JpaRepository<PackageVersion, Long> {
}
