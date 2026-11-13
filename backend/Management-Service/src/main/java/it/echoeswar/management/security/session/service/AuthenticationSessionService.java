package it.echoeswar.management.security.session.service;

import it.echoeswar.management.security.session.dto.response.ActiveSessionResponse;
import it.echoeswar.management.security.session.token.RefreshTokenRotationResult;
import it.echoeswar.management.security.session.config.RefreshTokenProperties;
import it.echoeswar.management.security.session.entity.AuthenticationSession;
import it.echoeswar.management.security.session.entity.RefreshToken;
import it.echoeswar.management.security.session.enumerated.SessionRevocationReason;
import it.echoeswar.management.security.session.exception.InvalidRefreshTokenException;
import it.echoeswar.management.security.session.repository.AuthenticationSessionRepository;
import it.echoeswar.management.security.session.repository.RefreshTokenRepository;
import it.echoeswar.management.security.session.token.GeneratedRefreshToken;
import it.echoeswar.management.security.session.token.IssuedRefreshToken;
import it.echoeswar.management.security.session.token.RefreshTokenGenerator;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages persistent authentication sessions and refresh-token rotation.
 *
 * <p>Sessions provide server-side revocation for otherwise stateless access tokens. Refresh
 * tokens are single-use credentials whose database records retain only a hash.</p>
 */
@Service
public class AuthenticationSessionService {

    private final AuthenticationSessionRepository authenticationSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenProperties refreshTokenProperties;
    private final UserRepository userRepository;

    public AuthenticationSessionService(
            AuthenticationSessionRepository authenticationSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenProperties refreshTokenProperties,
            UserRepository userRepository
    ) {
        this.authenticationSessionRepository = authenticationSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenProperties = refreshTokenProperties;
        this.userRepository = userRepository;
    }

    /**
     * Creates a session and its first refresh-token record for a user.
     *
     * <p>The raw refresh token is returned exactly once in the result; only its hash is stored in
     * the database. The returned expiration is shared by the session and its first token.</p>
     */
    @Transactional
    public IssuedRefreshToken createSession(
            UUID userId,
            String ipAddress,
            String userAgent
    ) {
        Objects.requireNonNull(userId, "userId must not be null");

        Instant now = Instant.now();
        Instant expiresAt = calculateExpiration(now);

        User userReference = userRepository.getReferenceById(userId);

        AuthenticationSession authenticationSession = AuthenticationSession.builder()
                .user(userReference)
                .createdAt(now)
                .lastUsedAt(now)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        authenticationSessionRepository.save(authenticationSession);

        GeneratedRefreshToken generatedRefreshToken = refreshTokenGenerator.generate();

        RefreshToken refreshToken = RefreshToken.builder()
                .session(authenticationSession)
                .tokenHash(generatedRefreshToken.tokenHash())
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(
                authenticationSession.getId(),
                generatedRefreshToken.rawToken(),
                expiresAt
        );
    }

    /**
     * Revokes one active session owned by the supplied user and every token in its family.
     *
     * <p>The session is locked before it is changed. A missing, expired, or already revoked
     * session is treated as a successful no-op so callers can safely repeat logout requests.</p>
     */
    @Transactional
    public void revokeSession(
            UUID userId,
            UUID sessionId,
            SessionRevocationReason revocationReason
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(
                revocationReason,
                "revocationReason must not be null"
        );

        Instant now = Instant.now();

        boolean activeSessionExists = authenticationSessionRepository
                .findActiveByIdAndUserIdForUpdate(sessionId, userId, now)
                .isPresent();

        if (!activeSessionExists) {
            return;
        }

        int revokedSessionCount =
                authenticationSessionRepository.revokeActiveSessionByIdAndUserId(
                        sessionId,
                        userId,
                        now,
                        revocationReason,
                        now
                );

        if (revokedSessionCount != 1) {
            throw new IllegalStateException(
                    "Authentication session could not be revoked."
            );
        }

        refreshTokenRepository.revokeTokensBySessionId(sessionId, now);
    }

    /**
     * Revokes all active sessions for a user together with their refresh-token families.
     *
     * <p>Active sessions are locked before the bulk update. If none remain, the operation is a
     * no-op.</p>
     */
    @Transactional
    public void revokeAllSessions(
            UUID userId,
            SessionRevocationReason revocationReason
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(
                revocationReason,
                "revocationReason must not be null"
        );

        Instant now = Instant.now();

        List<AuthenticationSession> activeSessions =
                authenticationSessionRepository.findActiveByUserIdForUpdate(
                        userId,
                        now
                );

        if (activeSessions.isEmpty()) {
            return;
        }

        List<UUID> sessionIds = activeSessions.stream()
                .map(AuthenticationSession::getId)
                .toList();

        int revokedSessionCount =
                authenticationSessionRepository.revokeActiveSessionsByIdsAndUserId(
                        sessionIds,
                        userId,
                        now,
                        revocationReason,
                        now
                );

        if (revokedSessionCount != sessionIds.size()) {
            throw new IllegalStateException(
                    "Authentication sessions could not be revoked."
            );
        }

        refreshTokenRepository.revokeTokensBySessionIds(sessionIds, now);
    }

    /**
     * Checks whether a session is still active for a user at the current instant.
     *
     * <p>This is used while converting a bearer token so an otherwise valid JWT cannot outlive a
     * server-side logout or session expiration.</p>
     */
    @Transactional(readOnly = true)
    public boolean isSessionActive(
            UUID userId,
            UUID sessionId
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        Instant now = Instant.now();

        return authenticationSessionRepository
                .findActiveByIdAndUserId(sessionId, userId, now)
                .isPresent();
    }

    /**
     * Consumes a refresh token exactly once and issues its replacement in the same session.
     *
     * <p>The presented token is conditionally consumed while the session is locked, then linked
     * to the newly persisted replacement. Reusing an already consumed token revokes the whole
     * session and its token family. {@link InvalidRefreshTokenException} intentionally does not
     * trigger rollback, preserving that defensive revocation.</p>
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshTokenRotationResult rotateRefreshToken(
            String rawRefreshToken
    ) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash = refreshTokenGenerator.hash(rawRefreshToken);
        Instant now = Instant.now();

        RefreshToken presentedRefreshToken = refreshTokenRepository
                .findByTokenHashWithSession(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        UUID tokenId = presentedRefreshToken.getId();
        UUID sessionId = presentedRefreshToken.getSession().getId();

        AuthenticationSession activeSession = authenticationSessionRepository
                .findActiveByIdForUpdate(sessionId, now)
                .orElseThrow(InvalidRefreshTokenException::new);

        UUID userId = activeSession.getUser().getId();

        int consumedTokenCount = refreshTokenRepository.consumeActiveByTokenHash(
                tokenHash,
                now,
                now
        );

        if (consumedTokenCount != 1) {
            handleUnconsumableRefreshToken(tokenHash, now);
        }

        Instant expiresAt = calculateExpiration(now);

        int renewedSessionCount = authenticationSessionRepository.renewActiveSession(
                sessionId,
                now,
                expiresAt,
                now
        );

        if (renewedSessionCount != 1) {
            throw new IllegalStateException(
                    "Authentication session could not be renewed."
            );
        }

        GeneratedRefreshToken generatedRefreshToken = refreshTokenGenerator.generate();

        AuthenticationSession sessionReference =
                authenticationSessionRepository.getReferenceById(sessionId);

        RefreshToken replacementRefreshToken = RefreshToken.builder()
                .session(sessionReference)
                .tokenHash(generatedRefreshToken.tokenHash())
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        RefreshToken savedReplacementRefreshToken =
                refreshTokenRepository.save(replacementRefreshToken);

        int linkedTokenCount = refreshTokenRepository.linkReplacement(
                tokenId,
                savedReplacementRefreshToken.getId()
        );

        if (linkedTokenCount != 1) {
            throw new IllegalStateException(
                    "Refresh token replacement could not be linked."
            );
        }

        return new RefreshTokenRotationResult(
                userId,
                new IssuedRefreshToken(
                        sessionId,
                        generatedRefreshToken.rawToken(),
                        expiresAt
                )
        );
    }

    /**
     * Returns the user's currently active sessions and marks the caller's session in each DTO.
     */
    @Transactional(readOnly = true)
    public List<ActiveSessionResponse> getActiveSessions(
            UUID userId,
            UUID currentSessionId
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(
                currentSessionId,
                "currentSessionId must not be null"
        );

        Instant now = Instant.now();

        return authenticationSessionRepository
                .findActiveByUserId(userId, now)
                .stream()
                .map(authenticationSession ->
                        ActiveSessionResponse.from(
                                authenticationSession,
                                currentSessionId
                        )
                )
                .toList();
    }

    private void handleUnconsumableRefreshToken(
            String tokenHash,
            Instant now
    ) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashWithSession(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.getConsumedAt() != null) {
            UUID sessionId = refreshToken.getSession().getId();

            authenticationSessionRepository.revokeSessionIfNotRevoked(
                    sessionId,
                    now,
                    SessionRevocationReason.TOKEN_REUSE
            );

            refreshTokenRepository.revokeTokensBySessionId(
                    sessionId,
                    now
            );
        }

        throw new InvalidRefreshTokenException();
    }

    private Instant calculateExpiration(Instant now) {
        Instant expiresAt = now.plus(refreshTokenProperties.ttl());

        if (!expiresAt.isAfter(now)) {
            throw new IllegalStateException(
                    "Refresh token TTL must be positive."
            );
        }

        return expiresAt;
    }
}
