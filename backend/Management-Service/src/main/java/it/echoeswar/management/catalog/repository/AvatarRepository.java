package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
