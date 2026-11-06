package it.echoeswar.management.user.repository;

import it.echoeswar.management.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            select u
            from User u
            join fetch u.role
            where lower(u.email) = lower(:email)
            """)
    Optional<User> findByEmailIgnoreCaseWithRole(@Param("email") String email);

    @Query("""
            select u
            from User u
            join fetch u.role
            where u.id = :id
            """)
    Optional<User> findByIdWithRole(@Param("id") UUID id);
}
