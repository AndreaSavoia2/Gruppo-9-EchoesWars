package it.echoeswar.management.security.session.repository;

import it.echoeswar.management.security.session.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}