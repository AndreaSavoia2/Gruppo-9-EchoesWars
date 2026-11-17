package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackageOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PackageOpeningRepository extends JpaRepository<PackageOpening, Long> {

    boolean existsByPackagePurchaseId(Long packagePurchaseId);

    /**
     * Conta le aperture completate dall'account per determinare la posizione nel ciclo di dieci.
     *
     * @param userId identificativo dell'account
     * @return numero totale di pacchetti già aperti dall'account
     */
    @Query("""
            select count(opening)
            from PackageOpening opening
            where opening.packagePurchase.creditTransaction.wallet.user.id = :userId
            """)
    long countByUserId(@Param("userId") UUID userId);
}
