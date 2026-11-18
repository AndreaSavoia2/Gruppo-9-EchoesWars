-- Ripristina lo schema dell'apertura fissa: cinque carte e ciclo da dieci.
ALTER TABLE package_openings
    DROP CONSTRAINT chk_package_opening_cycle_position;

ALTER TABLE package_openings
    ALTER COLUMN cycle_position TYPE SMALLINT;

ALTER TABLE package_openings
    ADD CONSTRAINT chk_package_opening_cycle_position CHECK (cycle_position BETWEEN 1 AND 10);

DROP TABLE package_purchase_opening_rule_snapshots;

DROP TABLE package_opening_rules;
