-- Keep the persisted schema aligned with the current game entities.
-- A challenge now owns the immutable rules version and simulation input used by its match.

-- Existing installations may not yet have a ruleset. Supply a deterministic default so every
-- historical challenge can receive a non-null rules_version_id during this migration.
INSERT INTO game_rule_versions (code, version, initial_life, max_rounds, is_active)
SELECT 'BASE', 1, 20, 100, TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM game_rule_versions
    WHERE is_active
)
ON CONFLICT (code, version) DO NOTHING;

ALTER TABLE challenges
    ADD COLUMN rules_version_id BIGINT,
    ADD COLUMN input_snapshot JSONB;

-- Preserve the ruleset and input used by already-created matches. Unplayed challenges receive
-- the latest active ruleset (or, if needed, the latest configured ruleset).
UPDATE challenges challenge
SET rules_version_id = COALESCE(
    (
        SELECT game_match.rules_version_id
        FROM matches game_match
        WHERE game_match.challenge_id = challenge.id
    ),
    (
        SELECT rule.id
        FROM game_rule_versions rule
        WHERE rule.is_active
        ORDER BY rule.id DESC
        LIMIT 1
    ),
    (
        SELECT rule.id
        FROM game_rule_versions rule
        ORDER BY rule.id DESC
        LIMIT 1
    )
);

UPDATE challenges challenge
SET input_snapshot = game_match.input_snapshot
FROM matches game_match
WHERE game_match.challenge_id = challenge.id
  AND game_match.input_snapshot IS NOT NULL;

ALTER TABLE challenges
    ALTER COLUMN rules_version_id SET NOT NULL,
    ADD CONSTRAINT fk_challenges_rules_version
        FOREIGN KEY (rules_version_id) REFERENCES game_rule_versions (id)
        ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX ix_challenges_rules_version_id
    ON challenges (rules_version_id);

-- Match now keeps only outcome and replay data; its historical input has been copied above.
ALTER TABLE matches
    DROP CONSTRAINT fk_matches_rules_version,
    DROP COLUMN rules_version_id,
    DROP COLUMN input_snapshot,
    DROP COLUMN random_seed;

-- Player level is no longer part of the User entity.
ALTER TABLE users
    DROP COLUMN level;
