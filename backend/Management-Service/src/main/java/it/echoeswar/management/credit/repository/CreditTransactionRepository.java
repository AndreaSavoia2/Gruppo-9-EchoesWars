package it.echoeswar.management.credit.repository;

import it.echoeswar.management.credit.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {
}
