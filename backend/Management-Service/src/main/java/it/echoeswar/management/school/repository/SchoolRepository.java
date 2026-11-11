package it.echoeswar.management.school.repository;

import it.echoeswar.management.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Byte> {
}
