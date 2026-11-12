ALTER TABLE cards
    DROP CONSTRAINT fk_cards_faction,
    DROP CONSTRAINT fk_cards_school,
    DROP CONSTRAINT fk_cards_tier,
    DROP CONSTRAINT fk_cards_effect;

ALTER TABLE faction_advantages
    DROP CONSTRAINT fk_faction_advantage_attacker,
    DROP CONSTRAINT fk_faction_advantage_defender;

ALTER TABLE deck_factions
    DROP CONSTRAINT fk_deck_factions_faction;

ALTER TABLE cards
    ALTER COLUMN faction_id TYPE SMALLINT USING faction_id::SMALLINT,
    ALTER COLUMN school_id TYPE SMALLINT USING school_id::SMALLINT,
    ALTER COLUMN tier_id TYPE SMALLINT USING tier_id::SMALLINT,
    ALTER COLUMN effect_id TYPE INTEGER USING effect_id::INTEGER;

ALTER TABLE faction_advantages
    ALTER COLUMN attacker_faction_id TYPE SMALLINT USING attacker_faction_id::SMALLINT,
    ALTER COLUMN defender_faction_id TYPE SMALLINT USING defender_faction_id::SMALLINT,
    ALTER COLUMN attack_bonus TYPE SMALLINT USING attack_bonus::SMALLINT;

ALTER TABLE deck_factions
    ALTER COLUMN faction_id TYPE SMALLINT USING faction_id::SMALLINT;

ALTER TABLE factions
    ALTER COLUMN id TYPE SMALLINT USING id::SMALLINT;

ALTER SEQUENCE factions_id_seq AS SMALLINT;

ALTER TABLE schools
    ALTER COLUMN id TYPE SMALLINT USING id::SMALLINT;

ALTER SEQUENCE schools_id_seq AS SMALLINT;

ALTER TABLE tiers
    ALTER COLUMN id TYPE SMALLINT USING id::SMALLINT;

ALTER SEQUENCE tiers_id_seq AS SMALLINT;

ALTER TABLE effects
    ALTER COLUMN id TYPE INTEGER USING id::INTEGER;

ALTER SEQUENCE effects_id_seq AS INTEGER;

ALTER TABLE cards
    ADD CONSTRAINT fk_cards_faction
        FOREIGN KEY (faction_id) REFERENCES factions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_cards_school
        FOREIGN KEY (school_id) REFERENCES schools(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_cards_tier
        FOREIGN KEY (tier_id) REFERENCES tiers(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_cards_effect
        FOREIGN KEY (effect_id) REFERENCES effects(id)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE faction_advantages
    ADD CONSTRAINT fk_faction_advantage_attacker
        FOREIGN KEY (attacker_faction_id) REFERENCES factions(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT fk_faction_advantage_defender
        FOREIGN KEY (defender_faction_id) REFERENCES factions(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE deck_factions
    ADD CONSTRAINT fk_deck_factions_faction
        FOREIGN KEY (faction_id) REFERENCES factions(id)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE users
    ALTER COLUMN account_status SET DEFAULT 'ACTIVE';
