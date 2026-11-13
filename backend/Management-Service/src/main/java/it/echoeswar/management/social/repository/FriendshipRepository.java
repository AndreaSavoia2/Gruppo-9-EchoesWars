package it.echoeswar.management.social.repository;

import it.echoeswar.management.social.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
}
