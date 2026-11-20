-- Replace the obsolete DRAFT status while it is still accepted by the old constraint.
UPDATE decks
SET status = 'READY'
WHERE status = 'DRAFT';

-- Keep the original migration immutable and update the constraint in a new Flyway migration.
ALTER TABLE decks
    DROP CONSTRAINT chk_decks_status;

-- A deck that was already soft-deleted is now explicitly marked as disabled.
UPDATE decks
SET status = 'DISABLED'
WHERE deleted_at IS NOT NULL;

ALTER TABLE decks
    ALTER COLUMN status SET DEFAULT 'READY';

ALTER TABLE decks
    ADD CONSTRAINT chk_decks_status
        CHECK (status IN ('READY', 'ARCHIVED', 'DISABLED'));
