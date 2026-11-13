package it.echoeswar.management.credit.repository;

import it.echoeswar.management.credit.entity.CreditWallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditWalletRepository extends JpaRepository<CreditWallet, Long> {
}
