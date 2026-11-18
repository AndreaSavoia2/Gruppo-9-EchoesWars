package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackagePurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, Long> {

    /**
     * Finds purchases owned by a user for which no opening has been recorded.
     *
     * <p>The package version and catalog package are fetched because the response needs both
     * objects after the read-only service transaction completes.</p>
     *
     * @param userId identifier of the purchase owner
     * @return unopened purchases ordered from most recently purchased to oldest
     */
    @EntityGraph(attributePaths = {"packageVersion", "packageVersion.gamePackage"})
    @Query("""
            select purchase
            from PackagePurchase purchase
            where purchase.creditTransaction.wallet.user.id = :userId
              and not exists (
                  select 1
                  from PackageOpening opening
                  where opening.packagePurchase = purchase
              )
            order by purchase.purchasedAt desc, purchase.id desc
            """)
    List<PackagePurchase> findUnopenedByUserId(@Param("userId") UUID userId);

    /**
     * Retrieves an unopened user purchase without applying a write lock.
     * It is used only by read operations that report progress toward the
     * guaranteed legendary.
     *
     * @param purchaseId purchase identifier
     * @param userId authenticated owner of the purchase
     * @return unopened purchase, if it belongs to the user
     */
    @Query("""
            select purchase
            from PackagePurchase purchase
            where purchase.id = :purchaseId
              and purchase.creditTransaction.wallet.user.id = :userId
              and not exists (
                  select 1
                  from PackageOpening opening
                  where opening.packagePurchase = purchase
              )
            """)
    Optional<PackagePurchase> findUnopenedByIdAndUserId(
            @Param("purchaseId") Long purchaseId,
            @Param("userId") UUID userId
    );

    /**
     * Retrieves and locks a purchase belonging to the user so it cannot be opened twice.
     *
     * @param purchaseId purchase identifier
     * @param userId authenticated owner of the purchase
     * @return locked purchase, if it exists and belongs to the user
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select purchase
            from PackagePurchase purchase
            where purchase.id = :purchaseId
              and purchase.creditTransaction.wallet.user.id = :userId
            """)
    Optional<PackagePurchase> findByIdAndUserIdForUpdate(
            @Param("purchaseId") Long purchaseId,
            @Param("userId") UUID userId
    );
}
