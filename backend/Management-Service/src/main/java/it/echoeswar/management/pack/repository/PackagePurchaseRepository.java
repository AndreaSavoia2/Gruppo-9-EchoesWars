package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, Long> {
}
