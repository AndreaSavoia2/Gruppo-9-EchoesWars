package it.echoeswar.management.security.jwt.service;

import it.echoeswar.management.security.jwt.config.JwtProperties;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues signed short-lived JWT access tokens for authenticated users.
 */
@Service
public class JwtTokenService {

    private static final String SESSION_ID_CLAIM = "sid";
    private static final String ROLES_CLAIM = "roles";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Creates an RS256 access token for an authenticated user session.
     */
    public String generateAccessToken(
            CustomUserDetails userDetails,
            UUID sessionId
    ) {
        Objects.requireNonNull(userDetails, "userDetails must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userDetails.getId().toString())
                .issuer(jwtProperties.issuer())
                .audience(List.of(jwtProperties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(SESSION_ID_CLAIM, sessionId.toString())
                .claim(ROLES_CLAIM, roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}