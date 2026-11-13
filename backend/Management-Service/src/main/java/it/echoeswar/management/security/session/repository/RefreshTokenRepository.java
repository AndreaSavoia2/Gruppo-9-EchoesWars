package it.echoeswar.management.security.session.repository;

import it.echoeswar.management.security.session.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("""
            select refreshToken
            from RefreshToken refreshToken
            join fetch refreshToken.session authenticationSession
            where refreshToken.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashWithSession(
            @Param("tokenHash") String tokenHash
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            update refresh_tokens refresh_token
            set consumed_at = :consumedAt
            from authentication_sessions authentication_session
            where refresh_token.token_hash = :tokenHash
              and refresh_token.session_id = authentication_session.id
              and refresh_token.consumed_at is null
              and refresh_token.revoked_at is null
              and refresh_token.expires_at > :now
              and authentication_session.revoked_at is null
              and authentication_session.expires_at > :now
            """, nativeQuery = true)
    int consumeActiveByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("consumedAt") Instant consumedAt,
            @Param("now") Instant now
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RefreshToken refreshToken
            set refreshToken.replacedByTokenId = :replacementTokenId
            where refreshToken.id = :tokenId
              and refreshToken.consumedAt is not null
              and refreshToken.replacedByTokenId is null
            """)
    int linkReplacement(
            @Param("tokenId") UUID tokenId,
            @Param("replacementTokenId") UUID replacementTokenId
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RefreshToken refreshToken
            set refreshToken.revokedAt = :revokedAt
            where refreshToken.session.id = :sessionId
              and refreshToken.revokedAt is null
            """)
    int revokeTokensBySessionId(
            @Param("sessionId") UUID sessionId,
            @Param("revokedAt") Instant revokedAt
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update RefreshToken refreshToken
        set refreshToken.revokedAt = :revokedAt
        where refreshToken.session.id in :sessionIds
          and refreshToken.revokedAt is null
        """)
    int revokeTokensBySessionIds(
            @Param("sessionIds") Collection<UUID> sessionIds,
            @Param("revokedAt") Instant revokedAt
    );
}