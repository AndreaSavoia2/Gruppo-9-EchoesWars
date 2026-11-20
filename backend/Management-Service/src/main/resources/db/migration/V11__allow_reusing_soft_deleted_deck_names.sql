-- A soft-deleted deck remains in history but no longer reserves its name.
ALTER TABLE decks
    DROP CONSTRAINT uq_decks_user_name;

CREATE UNIQUE INDEX ux_decks_user_name_not_deleted
    ON decks (user_id, name)
    WHERE deleted_at IS NULL;
