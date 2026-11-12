package it.echoeswar.management.catalog.repository;

import it.echoeswar.management.catalog.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Byte> {
}
