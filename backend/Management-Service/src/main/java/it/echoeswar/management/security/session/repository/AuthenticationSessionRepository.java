package it.echoeswar.management.security.session.repository;

import it.echoeswar.management.security.session.entity.AuthenticationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthenticationSessionRepository
        extends JpaRepository<AuthenticationSession, UUID> {

    @Query("""
            select authenticationSession
            from AuthenticationSession authenticationSession
            where authenticationSession.id = :sessionId
              and authenticationSession.user.id = :userId
              and authenticationSession.revokedAt is null
              and authenticationSession.expiresAt > :now
            """)
    Optional<AuthenticationSession> findActiveByIdAndUserId(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );

    @Query("""
            select authenticationSession
            from AuthenticationSession authenticationSession
            where authenticationSession.user.id = :userId
              and authenticationSession.revokedAt is null
              and authenticationSession.expiresAt > :now
            order by authenticationSession.lastUsedAt desc
            """)
    List<AuthenticationSession> findActiveByUserId(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );
}
