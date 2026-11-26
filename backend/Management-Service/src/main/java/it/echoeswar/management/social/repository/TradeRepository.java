package it.echoeswar.management.social.repository;

import it.echoeswar.management.social.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides persistence operations for card-trade proposals between friends.
 */
public interface TradeRepository extends JpaRepository<Trade, Long> {
}
