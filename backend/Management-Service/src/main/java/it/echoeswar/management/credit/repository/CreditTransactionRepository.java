package it.echoeswar.management.credit.repository;

import it.echoeswar.management.credit.entity.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {"wallet","wallet.user"}
    )
    Page<CreditTransaction> findAllByWalletUserId(UUID walletUserId, Pageable pageable);

}
