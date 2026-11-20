package it.echoeswar.management.user.repository;

import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

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
            left join fetch u.selectedAvatar
            where u.id = :id
            """)
    Optional<User> findByIdWithRole(@Param("id") UUID id);

    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))
              AND u.accountStatus = :accountStatus
              AND u.deletedAt IS NULL
            ORDER BY LOWER(u.username), u.id
            """)
    List<User> findAllVisibleByUsernameLike(
            @Param("username") String username,
            @Param("accountStatus") AccountStatus accountStatus
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from User u
            where lower(u.email) = lower(:email)
            """)
    Optional<User> findByEmailIgnoreCaseForUpdate(
            @Param("email") String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from User u
            where u.id = :userId
            """)
    Optional<User> findByIdForUpdate(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("""
            update User u
            set u.lastLoginAt = :lastLoginAt
            where u.id = :userId
              and u.accountStatus = :expectedAccountStatus
              and u.deletedAt is null
            """)
    int updateLastLoginAtIfAccountMatches(
            @Param("userId") UUID userId,
            @Param("lastLoginAt") Instant lastLoginAt,
            @Param("expectedAccountStatus") AccountStatus expectedAccountStatus
    );
}
