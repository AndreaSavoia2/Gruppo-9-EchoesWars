package it.echoeswar.management.security.session.repository;

import it.echoeswar.management.security.session.entity.AuthenticationSession;
import it.echoeswar.management.security.session.enumerated.SessionRevocationReason;
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

public interface AuthenticationSessionRepository
        extends JpaRepository<AuthenticationSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select authenticationSession
        from AuthenticationSession authenticationSession
        where authenticationSession.id = :sessionId
          and authenticationSession.revokedAt is null
          and authenticationSession.expiresAt > :now
        """)
    Optional<AuthenticationSession> findActiveByIdForUpdate(
            @Param("sessionId") UUID sessionId,
            @Param("now") Instant now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select authenticationSession
        from AuthenticationSession authenticationSession
        where authenticationSession.user.id = :userId
          and authenticationSession.revokedAt is null
          and authenticationSession.expiresAt > :now
        order by authenticationSession.id
        """)
    List<AuthenticationSession> findActiveByUserIdForUpdate(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select authenticationSession
        from AuthenticationSession authenticationSession
        where authenticationSession.id = :sessionId
          and authenticationSession.user.id = :userId
          and authenticationSession.revokedAt is null
          and authenticationSession.expiresAt > :now
        """)
    Optional<AuthenticationSession> findActiveByIdAndUserIdForUpdate(
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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update AuthenticationSession authenticationSession
            set authenticationSession.lastUsedAt = :lastUsedAt,
                authenticationSession.expiresAt = :expiresAt
            where authenticationSession.id = :sessionId
              and authenticationSession.revokedAt is null
              and authenticationSession.expiresAt > :now
            """)
    int renewActiveSession(
            @Param("sessionId") UUID sessionId,
            @Param("lastUsedAt") Instant lastUsedAt,
            @Param("expiresAt") Instant expiresAt,
            @Param("now") Instant now
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update AuthenticationSession authenticationSession
        set authenticationSession.revokedAt = :revokedAt,
            authenticationSession.revocationReason = :revocationReason
        where authenticationSession.id = :sessionId
          and authenticationSession.user.id = :userId
          and authenticationSession.revokedAt is null
          and authenticationSession.expiresAt > :now
        """)
    int revokeActiveSessionByIdAndUserId(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId,
            @Param("revokedAt") Instant revokedAt,
            @Param("revocationReason") SessionRevocationReason revocationReason,
            @Param("now") Instant now
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update AuthenticationSession authenticationSession
        set authenticationSession.revokedAt = :revokedAt,
            authenticationSession.revocationReason = :revocationReason
        where authenticationSession.id in :sessionIds
          and authenticationSession.user.id = :userId
          and authenticationSession.revokedAt is null
          and authenticationSession.expiresAt > :now
        """)
    int revokeActiveSessionsByIdsAndUserId(
            @Param("sessionIds") List<UUID> sessionIds,
            @Param("userId") UUID userId,
            @Param("revokedAt") Instant revokedAt,
            @Param("revocationReason") SessionRevocationReason revocationReason,
            @Param("now") Instant now
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update AuthenticationSession authenticationSession
            set authenticationSession.revokedAt = :revokedAt,
                authenticationSession.revocationReason = :revocationReason
            where authenticationSession.id = :sessionId
              and authenticationSession.revokedAt is null
            """)
    int revokeSessionIfNotRevoked(
            @Param("sessionId") UUID sessionId,
            @Param("revokedAt") Instant revokedAt,
            @Param("revocationReason") SessionRevocationReason revocationReason
    );
}