package it.echoeswar.management.collection.repository;

import it.echoeswar.management.collection.entity.UserCard;
import it.echoeswar.management.collection.entity.UserCardId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCardRepository extends JpaRepository<UserCard, UserCardId> {
}
