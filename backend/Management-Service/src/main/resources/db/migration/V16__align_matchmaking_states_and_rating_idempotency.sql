-- Allinea i nomi persistiti degli stati al contratto pubblico del matchmaking.
ALTER TABLE matchmaking_requests
    DROP CONSTRAINT IF EXISTS chk_matchmaking_requests_match_association,
    DROP CONSTRAINT IF EXISTS chk_matchmaking_requests_status;

DROP INDEX IF EXISTS ux_matchmaking_requests_waiting_user;
DROP INDEX IF EXISTS ix_matchmaking_requests_waiting_created;
DROP INDEX IF EXISTS ix_matchmaking_requests_waiting_expiration;

UPDATE matchmaking_requests
SET status = CASE status
    WHEN 'PENDING' THEN 'SEARCHING'
    WHEN 'COMBINED' THEN 'MATCHED'
    ELSE status
END;

ALTER TABLE matchmaking_requests
    ALTER COLUMN status SET DEFAULT 'SEARCHING',
    ADD CONSTRAINT chk_matchmaking_requests_status
        CHECK (status IN ('SEARCHING', 'MATCHED', 'CANCELLED', 'EXPIRED')),
    ADD CONSTRAINT chk_matchmaking_requests_match_association
        CHECK (
            (status = 'MATCHED' AND match_id IS NOT NULL)
            OR (
                status IN ('SEARCHING', 'CANCELLED', 'EXPIRED')
                AND match_id IS NULL
            )
        );

-- Una sola ricerca attiva per giocatore; le ricerche già abbinate restano nello storico.
CREATE UNIQUE INDEX ux_matchmaking_requests_searching_user
    ON matchmaking_requests (user_id)
    WHERE status = 'SEARCHING';

CREATE INDEX ix_matchmaking_requests_searching_created
    ON matchmaking_requests (created_at, id)
    WHERE status = 'SEARCHING';

CREATE INDEX ix_matchmaking_requests_searching_expiration
    ON matchmaking_requests (expires_at)
    WHERE status = 'SEARCHING';

-- Evita che un completamento ritentato applichi l'Elo una seconda volta.
ALTER TABLE matches
    ADD COLUMN rating_applied_at TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE matches
    ADD CONSTRAINT chk_matches_rating_applied_only_when_completed
        CHECK (rating_applied_at IS NULL OR status = 'COMPLETED');
