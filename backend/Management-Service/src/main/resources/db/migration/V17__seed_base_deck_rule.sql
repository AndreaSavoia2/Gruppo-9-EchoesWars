-- Base deck validation rule.
INSERT INTO deck_rules (
    id,
    code,
    name,
    exact_card_count,
    max_total_cost,
    max_copies_same_card,
    required_faction_count,
    is_active,
    created_at,
    updated_at
) VALUES (
    1,
    '1.0',
    'base_rule',
    30,
    98,
    3,
    2,
    TRUE,
    TIMESTAMP '2026-11-25 14:40:00.733',
    TIMESTAMP '2026-11-25 14:40:00.733'
)
ON CONFLICT (id) DO NOTHING;

-- The explicit identifier above does not advance PostgreSQL's identity sequence.
SELECT setval(
    pg_get_serial_sequence('deck_rules', 'id'),
    (SELECT MAX(id) FROM deck_rules),
    TRUE
);
