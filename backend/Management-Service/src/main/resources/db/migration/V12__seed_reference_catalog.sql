-- V12: reference catalog data required by the Management Service.
-- Keep this migration immutable after it has been applied.

-- Reference data
INSERT INTO roles (id, role, role_default) VALUES
    (1, 'ROLE_USER', TRUE),
    (2, 'ROLE_ADMIN', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO factions (id, code, name_it, name_en, color_hex) VALUES
    (1, 'DOMINION', 'Dominio', 'Dominion', '#B93428'),
    (2, 'WISDOM', 'Sapienza', 'Wisdom', '#1765B5'),
    (3, 'OBLIVION', 'Oblio', 'Oblivion', '#000000'),
    (4, 'DECEPTION', 'Inganno', 'Deception', '#237A43')
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name_it = EXCLUDED.name_it,
    name_en = EXCLUDED.name_en,
    color_hex = EXCLUDED.color_hex,
    is_active = TRUE;

INSERT INTO schools (id, code, name_it, name_en) VALUES
    (1, 'WARRIOR', 'Guerriero', 'Warrior'),
    (2, 'MAGE', 'Mago', 'Mage'),
    (3, 'ROGUE', 'Ladro', 'Rogue')
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name_it = EXCLUDED.name_it,
    name_en = EXCLUDED.name_en,
    is_active = TRUE;

INSERT INTO tiers (id, code, name_it, name_en, level) VALUES
    (1, 'COMMON', 'Comune', 'Common', 1),
    (2, 'RARE', 'Rara', 'Rare', 2),
    (3, 'LEGENDARY', 'Leggendaria', 'Legendary', 3)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name_it = EXCLUDED.name_it,
    name_en = EXCLUDED.name_en,
    level = EXCLUDED.level,
    is_active = TRUE;

INSERT INTO effects (id, code, name_it, name_en) VALUES
    (1, 'ASSAULT', 'Assalto', 'Assault'),
    (2, 'FURY', 'Furia', 'Fury'),
    (3, 'PIERCING', 'Perforante', 'Piercing'),
    (4, 'BERSERKER', 'Berserker', 'Berserker'),
    (5, 'RESISTANCE', 'Resistenza', 'Resistance'),
    (6, 'REFLECT', 'Riflesso', 'Reflect'),
    (7, 'SACRIFICE', 'Sacrificio', 'Sacrifice'),
    (8, 'GUARDIAN', 'Guardiano', 'Guardian'),
    (9, 'HEALING', 'Guarigione', 'Healing'),
    (10, 'DRAIN', 'Drenaggio', 'Drain'),
    (11, 'SABOTAGE', 'Sabotaggio', 'Sabotage'),
    (12, 'CURSE', 'Maledizione', 'Curse'),
    (13, 'MEMORY', 'Memoria', 'Memory'),
    (14, 'CONDEMNATION', 'Condanna', 'Condemnation'),
    (15, 'DISCIPLINE', 'Disciplina', 'Discipline'),
    (16, 'DUELIST', 'Duellante', 'Duelist'),
    (17, 'ARCANE_PREPARATION', 'Preparazione Arcana', 'Arcane Preparation'),
    (18, 'ANALYSIS', 'Analisi', 'Analysis'),
    (19, 'AMBUSH', 'Imboscata', 'Ambush'),
    (20, 'OPPORTUNIST', 'Opportunista', 'Opportunist')
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name_it = EXCLUDED.name_it,
    name_en = EXCLUDED.name_en,
    is_active = TRUE;

UPDATE effects e
SET description_it = v.description_it,
    description_en = v.description_en
FROM (VALUES
    ('ASSAULT', $it$Dal secondo round in campo, all’inizio di ogni round ottiene +1 ATK, cumulabile fino a +3 ATK. Il bonus persiste finché la carta resta in campo.$it$, $en$From its second round on the field, at the start of each round it gains +1 ATK, stacking up to +3 ATK. The bonus lasts as long as the card remains on the field.$en$),
    ('FURY', $it$Dopo uno scontro in cui riceve danni alla DEF e sopravvive, ottiene +1 ATK, fino a un massimo di +2. Il bonus è utilizzabile dal round successivo.$it$, $en$After a clash in which it takes damage to its DEF and survives, it gains +1 ATK, up to a maximum of +2. The bonus can be used from the next round.$en$),
    ('PIERCING', $it$I suoi attacchi ignorano le riduzioni dirette ai danni, come Resistenza, ma non ignorano DEF o Protezione.$it$, $en$Its attacks ignore direct damage reductions such as Resistance, but do not ignore DEF or Protection.$en$),
    ('BERSERKER', $it$Dal secondo round in campo, all’inizio del round perde 1 DEF residua per ottenere +2 ATK, fino a un massimo di +4 ATK. Non riduce la DEF sotto 1; se non può pagare la DEF, non ottiene il bonus.$it$, $en$From its second round on the field, at the start of the round it loses 1 current DEF to gain +2 ATK, up to a maximum of +4 ATK. It never reduces DEF below 1; if it cannot pay the DEF, it gains no bonus.$en$),
    ('RESISTANCE', $it$Per i primi due round in campo, riduce di 1 ogni danno subito da un attacco avversario (minimo 1). Non mitiga il danno di Riflesso né le riduzioni dirette di DEF di Maledizione.$it$, $en$For its first two rounds on the field, it reduces every damage taken from an enemy attack by 1 (minimum 1). It does not mitigate Reflect damage or the direct DEF reductions from Curse.$en$),
    ('REFLECT', $it$Una volta per round, quando subisce danni da un attacco, infligge 1 danno speciale alla carta avversaria, anche se viene distrutta. Il danno è assorbito da Protezione e poi da DEF, non causa overflow e non attiva altri Riflessi.$it$, $en$Once per round, when it takes damage from an attack, it deals 1 special damage to the opposing card, even if it is destroyed. The damage is absorbed by Protection and then DEF, causes no overflow and does not trigger other Reflects.$en$),
    ('SACRIFICE', $it$Quando la carta viene distrutta per qualsiasi causa, il suo proprietario recupera 2 PV (massimo 20). Non può evitare la sconfitta se il giocatore è già a 0 PV.$it$, $en$When the card is destroyed for any reason, its owner recovers 2 HP (maximum 20). It cannot prevent defeat if the player has already reached 0 HP.$en$),
    ('GUARDIAN', $it$Durante il primo round in campo riceve 2 Protezione, separate dalla DEF, che assorbono i danni prima della DEF. I punti non utilizzati scadono alla fine del primo round.$it$, $en$During its first round on the field it gains 2 Protection, separate from DEF, which absorbs damage before DEF. Unused points expire at the end of the first round.$en$),
    ('HEALING', $it$Alla fine di ogni round in cui rimane in campo, il proprietario recupera 1 PV, fino a un massimo di 3 PV totali grazie a questa carta e senza superare i 20 PV.$it$, $en$At the end of each round in which it remains on the field, its owner recovers 1 HP, up to a maximum of 3 HP in total from this card and never above 20 HP.$en$),
    ('DRAIN', $it$Quando sconfigge una carta avversaria e sopravvive alla risoluzione completa, l’avversario scarta la carta in cima al proprio mazzo. Non si attiva se la carta è distrutta da Riflesso.$it$, $en$When it defeats an enemy card and survives the full resolution, the opponent discards the top card of their deck. It does not trigger if the card is destroyed by Reflect.$en$),
    ('SABOTAGE', $it$Quando entra in campo, l’avversario scarta la carta in cima al proprio mazzo.$it$, $en$When it enters the field, the opponent discards the top card of their deck.$en$),
    ('CURSE', $it$Quando entra in campo, riduce di 2 la DEF residua della carta avversaria già in campo, fino a un minimo di 1. Non distrugge la carta né danneggia i PV; ignora la Protezione senza consumarla.$it$, $en$When it enters the field, it reduces the current DEF of the opposing card already on the field by 2, down to a minimum of 1. It does not destroy the card or damage HP; it ignores Protection without consuming it.$en$),
    ('MEMORY', $it$All’ingresso ottiene +1 ATK per ogni 5 carte nei propri scarti, fino a +3. Si calcola una sola volta, sullo stato precedente agli effetti di ingresso simultanei.$it$, $en$On entering the field it gains +1 ATK for every 5 cards in its owner’s discard pile, up to +3. Calculated only once, using the state before any simultaneous enter effects.$en$),
    ('CONDEMNATION', $it$Quando la carta viene distrutta per qualsiasi causa, l’avversario scarta la prima carta del proprio mazzo, se disponibile. Si attiva anche nelle distruzioni simultanee, se la partita continua.$it$, $en$When the card is destroyed for any reason, the opponent discards the first card of their deck, if available. It also triggers on simultaneous destructions, if the game continues.$en$),
    ('DISCIPLINE', $it$All’ingresso ottiene +1 DEF permanente se l’ultima carta alleata effettivamente distrutta in campo apparteneva alla Scuola Guerrieri. Gli scarti diretti dal mazzo non contano.$it$, $en$On entering the field it gains a permanent +1 DEF if the last allied card actually destroyed on the field belonged to the Warrior class. Cards discarded directly from the deck do not count.$en$),
    ('DUELIST', $it$Ottiene +1 ATK quando affronta una carta della Scuola Maghi. Il bonus non è cumulabile e viene rivalutato a inizio round dopo gli effetti di ingresso.$it$, $en$Gains +1 ATK when facing a Mage card. The bonus does not stack and is re-evaluated at the start of each round after enter effects.$en$),
    ('ARCANE_PREPARATION', $it$All’ingresso ottiene +1 ATK permanente se l’ultima carta alleata effettivamente distrutta in campo apparteneva alla Scuola Maghi. Gli scarti diretti dal mazzo non contano.$it$, $en$On entering the field it gains a permanent +1 ATK if the last allied card actually destroyed on the field belonged to the Mage class. Cards discarded directly from the deck do not count.$en$),
    ('ANALYSIS', $it$Contro un avversario con ATK base stampato maggiore del proprio, ottiene 1 punto di Protezione una sola volta per quell’avversario. La Protezione dura fino al consumo o alla fine dell’incontro.$it$, $en$Against an opponent with a higher printed base ATK, it gains 1 Protection point, only once per opponent. The Protection lasts until consumed or until the end of the encounter.$en$),
    ('AMBUSH', $it$Ottiene +2 ATK soltanto nel primo round trascorso in campo; dal round seguente il bonus scompare.$it$, $en$Gains +2 ATK only during its first round on the field; from the following round the bonus disappears.$en$),
    ('OPPORTUNIST', $it$Ottiene +1 ATK fisso, non cumulabile, se la DEF residua avversaria è inferiore alla sua DEF massima effettiva. Verificato a inizio round dopo gli effetti di ingresso; la sola perdita di Protezione non basta.$it$, $en$Gains a fixed +1 ATK, non-stacking, if the opponent’s current DEF is lower than its maximum effective DEF. Checked at the start of the round after enter effects; merely losing Protection is not enough.$en$)
) AS v(code, description_it, description_en)
WHERE e.code = v.code;

-- Card catalog. Faction, school, tier and effect are resolved by code so that
-- the script does not depend on generated identifiers.
WITH card_data(code, name_it, name_en, description_it, description_en, attack, defense, play_cost, faction_code, school_code, tier_code, effect_code) AS (
    VALUES
    ('DOM01', 'Cadetto', 'Dominion Cadet', 'Giovane recluta del Dominio, ancora inesperta ma pronta a difendere la Corona con il suo giuramento appena pronunciato.', 'A young recruit of the Dominion, still inexperienced but ready to defend the Crown with a freshly sworn oath.', 5, 4, 3, 'DOMINION', 'WARRIOR', 'COMMON', NULL),
    ('DOM02', 'Scudiero del Bastione', 'Squire of the Bastion', NULL, NULL, 3, 3, 3, 'DOMINION', 'WARRIOR', 'COMMON', 'RESISTANCE'),
    ('DOM03', 'Lanciera della Corona', 'Crown Lancer', NULL, NULL, 5, 4, 3, 'DOMINION', 'WARRIOR', 'COMMON', 'DUELIST'),
    ('DOM04', 'Guardia della Porta', 'Gate Guard', NULL, NULL, 4, 6, 4, 'DOMINION', 'WARRIOR', 'COMMON', 'GUARDIAN'),
    ('DOM05', 'Veterano dell’Assedio', 'Siege Veteran', NULL, NULL, 6, 4, 4, 'DOMINION', 'WARRIOR', 'RARE', 'FURY'),
    ('DOM06', 'Martello del Re', 'King’s Hammer', NULL, NULL, 7, 4, 5, 'DOMINION', 'WARRIOR', 'LEGENDARY', 'PIERCING'),
    ('DOM07', 'Campione delle Mura', 'Champion of the Walls', NULL, NULL, 6, 6, 6, 'DOMINION', 'WARRIOR', 'RARE', 'ASSAULT'),
    ('DOM08', 'Furia del Trono', 'Fury of the Throne', NULL, NULL, 7, 5, 6, 'DOMINION', 'WARRIOR', 'LEGENDARY', 'GUARDIAN'),
    ('DOM09', 'Araldo della Vittoria', 'Herald of Victory', NULL, NULL, 4, 4, 3, 'DOMINION', 'MAGE', 'COMMON', 'ARCANE_PREPARATION'),
    ('DOM10', 'Alchimista d’Assedio', 'Siege Alchemist', NULL, NULL, 4, 5, 4, 'DOMINION', 'MAGE', 'RARE', 'CURSE'),
    ('DOM11', 'Spia del Bastione', 'Bastion Spy', NULL, NULL, 5, 3, 3, 'DOMINION', 'ROGUE', 'COMMON', 'ASSAULT'),
    ('DOM12', 'Predone Corazzato', 'Armored Raider', NULL, NULL, 6, 3, 4, 'DOMINION', 'ROGUE', 'RARE', 'AMBUSH'),
    ('SAP01', 'Allievo delle Rune', 'Rune Pupil', 'Studia le rune antiche con dedizione, tracciando i primi segni di un sapere ancora tutto da padroneggiare.', 'Studies the ancient runes with devotion, tracing the first signs of a knowledge yet to be mastered.', 4, 4, 3, 'WISDOM', 'MAGE', 'COMMON', NULL),
    ('SAP02', 'Custode dei Tomi', 'Keeper of Tomes', NULL, NULL, 3, 8, 4, 'WISDOM', 'MAGE', 'RARE', 'ANALYSIS'),
    ('SAP03', 'Novizia del Sigillo', 'Novice of the Seal', NULL, NULL, 4, 8, 5, 'WISDOM', 'MAGE', 'RARE', 'MEMORY'),
    ('SAP04', 'Geometra Arcano', 'Arcane Geometer', 'Misura le linee invisibili della magia: ogni incantesimo, per lui, è un teorema da dimostrare.', 'Measures the invisible lines of magic: to him, every spell is a theorem waiting to be proven.', 4, 4, 3, 'WISDOM', 'MAGE', 'COMMON', NULL),
    ('SAP05', 'Mistico della Soglia', 'Mystic of the Threshold', NULL, NULL, 5, 7, 5, 'WISDOM', 'MAGE', 'RARE', 'HEALING'),
    ('SAP06', 'Sapiente dei Riflessi', 'Sage of Reflections', NULL, NULL, 5, 4, 4, 'WISDOM', 'MAGE', 'COMMON', 'REFLECT'),
    ('SAP07', 'Oracolo del Vuoto', 'Oracle of the Void', NULL, NULL, 6, 5, 6, 'WISDOM', 'MAGE', 'LEGENDARY', 'CURSE'),
    ('SAP08', 'Maestro delle Ere', 'Master of Ages', NULL, NULL, 5, 7, 6, 'WISDOM', 'MAGE', 'LEGENDARY', 'ASSAULT'),
    ('SAP09', 'Custode del Codice', 'Keeper of the Code', NULL, NULL, 4, 3, 3, 'WISDOM', 'WARRIOR', 'COMMON', 'DISCIPLINE'),
    ('SAP10', 'Guardiana degli Archivi', 'Guardian of the Archives', NULL, NULL, 4, 6, 4, 'WISDOM', 'WARRIOR', 'RARE', 'GUARDIAN'),
    ('SAP11', 'Esploratrice del Sapere', 'Explorer of Knowledge', NULL, NULL, 5, 2, 3, 'WISDOM', 'ROGUE', 'COMMON', 'OPPORTUNIST'),
    ('SAP12', 'Segugio dei Segreti', 'Hound of Secrets', 'Fiuta le verità nascoste tra gli scaffali e le ombre: nessun segreto gli resta celato a lungo.', 'Sniffs out the truths hidden among the shelves and shadows: no secret stays concealed from it for long.', 5, 4, 3, 'WISDOM', 'ROGUE', 'COMMON', NULL),
    ('OBL01', 'Eco Svanito', 'Faded Echo', NULL, NULL, 3, 4, 3, 'OBLIVION', 'ROGUE', 'COMMON', 'SACRIFICE'),
    ('OBL02', 'Ombra dei Sepolcri', 'Shadow of the Sepulchers', 'Un’ombra silenziosa che vaga tra le tombe dimenticate, custode di ciò che nessuno ricorda più.', 'A silent shadow wandering among forgotten tombs, keeper of what no one remembers anymore.', 4, 4, 3, 'OBLIVION', 'ROGUE', 'COMMON', NULL),
    ('OBL03', 'Ladra di Ricordi', 'Memory Thief', NULL, NULL, 3, 5, 3, 'OBLIVION', 'ROGUE', 'COMMON', 'MEMORY'),
    ('OBL04', 'Predatore del Silenzio', 'Predator of Silence', NULL, NULL, 4, 4, 3, 'OBLIVION', 'ROGUE', 'COMMON', 'OPPORTUNIST'),
    ('OBL05', 'Spettro della Cripta', 'Crypt Specter', NULL, NULL, 3, 4, 3, 'OBLIVION', 'MAGE', 'COMMON', 'CURSE'),
    ('OBL06', 'Cultista del Crepuscolo', 'Twilight Cultist', NULL, NULL, 5, 6, 4, 'OBLIVION', 'MAGE', 'RARE', 'SACRIFICE'),
    ('OBL07', 'Stregone dei Nomi', 'Warlock of Names', NULL, NULL, 5, 4, 3, 'OBLIVION', 'MAGE', 'RARE', 'DRAIN'),
    ('OBL08', 'Lamento dell’Abisso', 'Lament of the Abyss', NULL, NULL, 5, 4, 4, 'OBLIVION', 'MAGE', 'RARE', 'REFLECT'),
    ('OBL09', 'Custode delle Tombe', 'Tomb Keeper', NULL, NULL, 3, 4, 3, 'OBLIVION', 'WARRIOR', 'COMMON', 'RESISTANCE'),
    ('OBL10', 'Cavaliere Sepolcrale', 'Sepulchral Knight', NULL, NULL, 5, 5, 4, 'OBLIVION', 'WARRIOR', 'LEGENDARY', 'FURY'),
    ('OBL11', 'Boia del Crepuscolo', 'Twilight Executioner', NULL, NULL, 6, 4, 4, 'OBLIVION', 'WARRIOR', 'LEGENDARY', 'BERSERKER'),
    ('OBL12', 'Sentinella Perduta', 'Lost Sentinel', NULL, NULL, 4, 5, 4, 'OBLIVION', 'WARRIOR', 'RARE', 'GUARDIAN'),
    ('ING01', 'Borseggiatore del Velo', 'Veil Pickpocket', 'Agile e invisibile tra la folla, svuota le tasche prima che la vittima si accorga di qualcosa.', 'Nimble and unseen in the crowd, empties pockets before the victim notices a thing.', 3, 4, 2, 'DECEPTION', 'ROGUE', 'COMMON', NULL),
    ('ING02', 'Messaggero Mascherato', 'Masked Messenger', 'Consegna messaggi senza mai mostrare il volto: nessuno sa per chi lavori davvero.', 'Delivers messages without ever showing a face: no one knows who he really works for.', 4, 4, 3, 'DECEPTION', 'ROGUE', 'COMMON', NULL),
    ('ING03', 'Doppio Gioco', 'Double Cross', NULL, NULL, 3, 5, 3, 'DECEPTION', 'ROGUE', 'COMMON', 'OPPORTUNIST'),
    ('ING04', 'Lama Velata', 'Veiled Blade', NULL, NULL, 5, 5, 4, 'DECEPTION', 'ROGUE', 'RARE', 'DRAIN'),
    ('ING05', 'Truffatore di Corte', 'Court Swindler', 'Sorride ai nobili mentre li alleggerisce del loro oro, con una lingua più affilata di qualsiasi lama.', 'Smiles at nobles while lightening their purses, with a tongue sharper than any blade.', 4, 5, 3, 'DECEPTION', 'ROGUE', 'COMMON', NULL),
    ('ING06', 'Predone delle Ombre', 'Shadow Raider', NULL, NULL, 5, 3, 3, 'DECEPTION', 'ROGUE', 'RARE', 'DRAIN'),
    ('ING07', 'Maestro delle Maschere', 'Master of Masks', NULL, NULL, 6, 6, 5, 'DECEPTION', 'ROGUE', 'LEGENDARY', 'PIERCING'),
    ('ING08', 'Acrobata dell’Ultimo Atto', 'Acrobat of the Final Act', NULL, NULL, 5, 6, 5, 'DECEPTION', 'ROGUE', 'RARE', 'REFLECT'),
    ('ING09', 'Illusionista dei Vicoli', 'Alley Illusionist', NULL, NULL, 4, 4, 3, 'DECEPTION', 'MAGE', 'COMMON', 'ANALYSIS'),
    ('ING10', 'Incantatore del Dubbio', 'Enchanter of Doubt', NULL, NULL, 4, 5, 4, 'DECEPTION', 'MAGE', 'RARE', 'CURSE'),
    ('ING11', 'Guardia Infiltrata', 'Infiltrated Guard', NULL, NULL, 3, 4, 3, 'DECEPTION', 'WARRIOR', 'COMMON', 'DUELIST'),
    ('ING12', 'Duellante dell’Ombra', 'Shadow Duelist', NULL, NULL, 5, 7, 6, 'DECEPTION', 'WARRIOR', 'LEGENDARY', 'ASSAULT')
)
INSERT INTO cards (
    code, name_it, name_en, description_it, description_en, attack, defense,
    play_cost, max_copies_per_deck, faction_id, school_id, tier_id, effect_id
)
SELECT
    c.code, c.name_it, c.name_en, c.description_it, c.description_en,
    c.attack, c.defense, c.play_cost, 3, f.id, s.id, t.id, e.id
FROM card_data c
JOIN factions f ON f.code = c.faction_code
JOIN schools s ON s.code = c.school_code
JOIN tiers t ON t.code = c.tier_code
LEFT JOIN effects e ON e.code = c.effect_code
ON CONFLICT (code) DO UPDATE SET
    name_it = EXCLUDED.name_it,
    name_en = EXCLUDED.name_en,
    description_it = EXCLUDED.description_it,
    description_en = EXCLUDED.description_en,
    attack = EXCLUDED.attack,
    defense = EXCLUDED.defense,
    play_cost = EXCLUDED.play_cost,
    faction_id = EXCLUDED.faction_id,
    school_id = EXCLUDED.school_id,
    tier_id = EXCLUDED.tier_id,
    effect_id = EXCLUDED.effect_id,
    is_active = TRUE;

INSERT INTO packages (id, code, name_it, name_en, description_it, description_en) VALUES
    (1, 'BASE_PACK', 'Pacchetto Base', 'Base Pack', 'Pacchetto standard di carte.', 'Standard card pack.')
ON CONFLICT (id) DO NOTHING;

-- V5 removed package_versions.pull_rule_code. Rules now live in package_pull_rules.
INSERT INTO package_versions (id, package_id, version_number, price_credits, is_active, published_at) VALUES
    (1, 1, 1, 300, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO package_pull_rules (
    id, package_version_id, cards_per_package, guaranteed_common_cards,
    guaranteed_rare_cards, cycle_length, random_common_threshold, random_rare_threshold
) VALUES
    (1, 1, 5, 3, 1, 10, 85, 98)
ON CONFLICT (id) DO NOTHING;



