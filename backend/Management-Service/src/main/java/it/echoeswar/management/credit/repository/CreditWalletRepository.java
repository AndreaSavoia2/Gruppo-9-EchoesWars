package it.echoeswar.management.credit.repository;

import it.echoeswar.management.credit.entity.CreditWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CreditWalletRepository extends JpaRepository<CreditWallet, Long> {

    Optional<CreditWallet> findByUserId(UUID userId);

    /**
     * Locks a wallet while a balance-changing operation is in progress.
     *
     * @param userId identifier of the wallet owner
     * @return the locked wallet and its owner, when present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select wallet
            from CreditWallet wallet
            join fetch wallet.user user
            where user.id = :userId
            """)
    Optional<CreditWallet> findByUserIdForUpdate(@Param("userId") UUID userId);
}
