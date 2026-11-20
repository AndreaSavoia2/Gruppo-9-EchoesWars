package it.echoeswar.management.social.repository;

import it.echoeswar.management.social.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Verifica se esiste già una relazione per la coppia di utenti nell'ordine
     * canonico previsto dalla tabella {@code friendships}.
     */
    boolean existsByUserA_IdAndUserB_Id(UUID userAId, UUID userBId);
}
