package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackageOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PackageOpeningRepository extends JpaRepository<PackageOpening, Long> {

    boolean existsByPackagePurchaseId(Long packagePurchaseId);

    /**
     * Counts completed account openings to determine the position in the cycle
     * defined by the rule of the version being opened.
     *
     * @param userId account identifier
     * @return total number of packages already opened by the account
     */
    @Query("""
            select count(opening)
            from PackageOpening opening
            where opening.packagePurchase.creditTransaction.wallet.user.id = :userId
            """)
    long countByUserId(@Param("userId") UUID userId);
}
