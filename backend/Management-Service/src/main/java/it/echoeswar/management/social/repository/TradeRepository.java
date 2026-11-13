package it.echoeswar.management.social.repository;

import it.echoeswar.management.social.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
