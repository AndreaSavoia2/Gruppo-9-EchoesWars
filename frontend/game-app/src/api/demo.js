// 🧪 DEMO MODE — fake backend, only to try the app WITHOUT the real backend.
// It is turned on with VITE_DEMO=true (command: npm run demo). With the real backend it is NOT used.
//
// It covers the "random match" flow:
//   Home → Play → Choose the deck → Matchmaking → Battle → You won/lost → Details
// The other pages (Collection, Trades...) show the error message in demo: that is expected.
//
// The data lives in memory: reloading the page (F5) starts again from scratch.

const MATCHMAKING_WAIT_MS = 3000 // how long "Stiamo simulando la partita" (simulating the match) lasts
const STARTING_HP = 10
const MAX_ROUNDS = 100
const CREDITS = { victory: 10, draw: 5, defeat: 2 }

// The 8 sample cards from the team's card.html file (frame + illustration).
// Every card uses its local illustration in public/images/illustrations (named after the code).
// [code, name, atk, def, cost, echo, school, rarity, effect, description]
export const SAMPLE_CARDS = [
  ['DOM01', 'Cadetto', 5, 4, 3, 'DOMINION', 'WARRIOR', 'COMMON', null, 'Giovane recluta del Dominio, ancora inesperta ma pronta a difendere la Corona con il suo giuramento appena pronunciato.'],
  ['DOM06', 'Martello del Re', 7, 4, 5, 'DOMINION', 'WARRIOR', 'LEGENDARY', 'PIERCING', null],
  ['SAP02', 'Custode dei Tomi', 3, 8, 4, 'WISDOM', 'MAGE', 'RARE', 'ANALYSIS', null],
  ['SAP07', 'Oracolo del Vuoto', 6, 5, 6, 'WISDOM', 'MAGE', 'LEGENDARY', 'CURSE', null],
  ['OBL02', 'Ombra dei Sepolcri', 4, 4, 3, 'OBLIVION', 'ROGUE', 'COMMON', null, 'Un’ombra silenziosa che vaga tra le tombe dimenticate, custode di ciò che nessuno ricorda più.'],
  ['OBL11', 'Boia del Crepuscolo', 6, 4, 4, 'OBLIVION', 'WARRIOR', 'LEGENDARY', 'BERSERKER', null],
  ['ING04', 'Lama Velata', 5, 5, 4, 'DECEPTION', 'ROGUE', 'RARE', 'DRAIN', null],
  ['ING01', 'Borseggiatore del Velo', 3, 4, 2, 'DECEPTION', 'ROGUE', 'COMMON', null, 'Agile e invisibile tra la folla, svuota le tasche prima che la vittima si accorga di qualcosa.'],
].map(([code, name, atk, def, cost, echo, school, rarity, effect, description]) => ({
  code, name, atk, def, cost, echo, school, rarity, effect, description,
  illustration: `/images/illustrations/${code}.webp`,
}))

// ---------- Player data ----------
const status = {
  user: { name: 'Flavio', credits: 1250, avatar: null, firstLogin: false },
  matches: [], // the matches played in this session
}

const decks = [
  { id: 1, name: 'Mazzo del tramonto', valid: true, reason: null, cardCount: 30, maxCards: 30, points: 96, maxPoints: 98, schools: ['Sapienza', 'Oblio'] },
  { id: 2, name: 'Lama e Velo', valid: true, reason: null, cardCount: 30, maxCards: 30, points: 92, maxPoints: 98, schools: ['Inganno'] },
  { id: 3, name: 'Furia Rossa', valid: true, reason: null, cardCount: 30, maxCards: 30, points: 97, maxPoints: 98, schools: ['Dominio'] },
  { id: 4, name: 'Custodi', valid: false, reason: '28/30 carte', cardCount: 28, maxCards: 30, points: 80, maxPoints: 98, schools: [] },
]

const opponents = ['@lyra_eco', '@ombra_sette', '@kael', '@nyx']

// ---------- The battle (simplified version of the rules) ----------
// Every round:
//   1. whoever has no card on the field draws from the deck
//   2. the two cards hit each other AT THE SAME TIME: DEF = DEF − opponent ATK
//   3. a card with DEF below 0 is destroyed and goes to the discards;
//      the extra damage (e.g. DEF −2 → 2) is lost by the player as HP
//   4. it goes on until someone reaches 0 HP, the cards run out or round 100 is reached

// A random number between min and max (included)
const randomPick = (min, max) => min + Math.floor(Math.random() * (max - min + 1))

/**
 * A deck of 30 cards drawn at random from the sample ones.
 * ({ ...card } = a COPY: this way the DEF that drops in battle does not change the original)
 * @returns {object[]}
 */
function createDeck() {
  return Array.from({ length: 30 }, () => {
    const card = SAMPLE_CARDS[randomPick(0, SAMPLE_CARDS.length - 1)]
    return { ...card, printedDef: card.def } // printedDef = the DEF written on the card
  })
}

/**
 * "Snapshot" of one side of the field, in the format expected by the Battle page.
 * @param {object} side - hp, deck, discards and card on the field of one player
 * @returns {object}
 */
function snapshot(side) {
  return {
    hp: Math.max(side.hp, 0),
    deckCount: side.deck.length,
    discardCount: side.discards.length,
    // All the card data: the page draws it in layers (frame + illustration + texts)
    lastDiscarded: side.discards.at(-1) ? { ...side.discards.at(-1), def: side.discards.at(-1).printedDef } : null,
    card: side.field ? { ...side.field } : null, // with the current DEF
  }
}

/**
 * Plays a whole match with the simplified rules and records every round for the replay.
 * @param {string} opponentName
 * @returns {object} final result + the rounds of the replay
 */
function simulateBattle(opponentName) {
  const you = { hp: STARTING_HP, deck: createDeck(), discards: [], field: null }
  const opp = { hp: STARTING_HP, deck: createDeck(), discards: [], field: null }
  const round = []

  for (let count = 1; count <= MAX_ROUNDS; count++) {
    // 1. draw (if needed)
    if (!you.field) you.field = you.deck.pop() ?? null
    if (!opp.field) opp.field = opp.deck.pop() ?? null
    if (!you.field || !opp.field) break // no cards left

    const beginning = { you: snapshot(you), opponent: snapshot(opp) }

    // 2. clash: they hit each other together
    const yourDamage = opp.field.atk // damage taken by YOUR card
    const opponentDamage = you.field.atk
    you.field.def -= yourDamage
    opp.field.def -= opponentDamage

    // 3. destroyed cards and excess damage
    const yourExcess = destroyIfNeeded(you)
    const oppExcess = destroyIfNeeded(opp)

    round.push({
      count,
      ...beginning,
      yourDamage,
      opponentDamage,
      outcome: outcomeText(yourExcess, oppExcess, opponentName),
      after: { you: snapshot(you), opponent: snapshot(opp) },
    })

    // 4. is anybody at 0 HP?
    if (you.hp <= 0 || opp.hp <= 0) break
  }

  return { round, you, opp }
}

/**
 * If the card on the field has DEF below 0: it goes to the discards and the player loses HP.
 * @param {object} side
 * @returns {number|null} null if the card is still alive, otherwise the lost HP
 */
function destroyIfNeeded(side) {
  if (side.field.def >= 0) return null
  const excess = -side.field.def
  side.hp -= excess
  side.discards.push(side.field)
  side.field = null
  return excess
}

/**
 * End of round message shown in the battle (in Italian, it is read by the user).
 * @param {number|null} yourExcess - HP lost by you (null = your card survived)
 * @param {number|null} oppExcess - HP lost by the opponent (null = their card survived)
 * @param {string} opponentName
 * @returns {{ title: string, detail: string }|null}
 */
function outcomeText(yourExcess, oppExcess, opponentName) {
  const lost = (n) => (n > 0 ? `perdi ${n} PV` : 'nessun PV perso')
  if (yourExcess !== null && oppExcess !== null) {
    return { title: 'Entrambe le carte sono distrutte', detail: `Tu: ${lost(yourExcess)} · ${opponentName}: −${oppExcess} PV` }
  }
  if (yourExcess !== null) return { title: 'La tua carta è distrutta', detail: `Danno in eccesso: ${lost(yourExcess)}` }
  if (oppExcess !== null) {
    return { title: 'Carta avversaria distrutta', detail: `Danno in eccesso: ${opponentName} perde ${oppExcess} PV` }
  }
  return null // no card destroyed: no message
}

// ---------- Creating a match ----------
/**
 * Creates a new demo match and simulates it straight away.
 * @param {string|number} deckId
 * @returns {object} the match saved in memory
 */
function startMatch(deckId) {
  const deck = decks.find((m) => m.id === deckId) ?? decks[0]
  const opponentName = opponents[randomPick(0, opponents.length - 1)]
  const { round, you, opp } = simulateBattle(opponentName)

  const outcome = you.hp > opp.hp ? 'victory' : you.hp < opp.hp ? 'defeat' : 'draw'
  const last = round.at(-1)
  const match = {
    id: status.matches.length + 1,
    startedAt: Date.now(),
    creditsGiven: false,
    myDeck: deck.name,
    opponent: { name: opponentName, deck: decks[randomPick(0, 2)].name },
    outcome,
    myHp: Math.max(you.hp, 0),
    opponentHp: Math.max(opp.hp, 0),
    creditsEarned: CREDITS[outcome],
    round: last.count,
    roundMax: MAX_ROUNDS,
    playedOn: new Date().toISOString(),
    myCardsLeft: you.deck.length,
    opponentCardsLeft: opp.deck.length,
    endReason: you.hp <= 0 || opp.hp <= 0 ? 'PV a 0' : last.count === MAX_ROUNDS ? 'Round 100' : 'Carte finite',
    replay: { roundMax: MAX_ROUNDS, opponent: { name: opponentName }, round },
  }
  status.matches.unshift(match)
  return match
}

/**
 * How GET /matches/:id answers: "in_progress" for the first seconds, then "finished".
 * @param {object} match - the match saved in memory
 * @returns {object}
 */
function readMatch(match) {
  const base = { id: match.id, myDeck: match.myDeck, opponent: match.opponent }
  if (Date.now() - match.startedAt < MATCHMAKING_WAIT_MS) return { ...base, status: 'in_progress' }

  // The first time it turns out finished: add the credits to the user
  if (!match.creditsGiven) {
    status.user.credits += match.creditsEarned
    match.creditsGiven = true
  }
  // We send everything except the "internal" demo fields (replay, time, credits given)
  const data = { ...match }
  delete data.replay
  delete data.startedAt
  delete data.creditsGiven
  return { ...data, status: 'finished' }
}

// The Echoes of a deck in the backend format: [{ id, nameIt }]
const DEMO_ECHO_IDS = { Dominio: 1, Sapienza: 2, Oblio: 3, Inganno: 4 }
const factionList = (m) => m.schools.map((name) => ({ id: DEMO_ECHO_IDS[name], nameIt: name }))

// ---------- The "switchboard": path → response ----------
/**
 * Receives the same things as a real fetch and returns the data (or throws a 404 error).
 * @param {string} method - 'GET', 'POST'...
 * @param {string} path - e.g. "/matches/3/replay"
 * @param {object} [body]
 * @returns {Promise<any>}
 */
export async function demoResponse(method, path, body) {
  await new Promise((resolve) => setTimeout(resolve, 250)) // small delay, like a real network
  const [address, query] = path.split('?')
  const params = new URLSearchParams(query)
  const pieces = address.split('/').filter(Boolean) // "/matches/3/replay" → ["matches", "3", "replay"]
  const request = `${method} /${pieces[0] ?? ''}`

  const finishedOnes = () => status.matches.filter((p) => Date.now() - p.startedAt >= MATCHMAKING_WAIT_MS)

  // ---- Responses in the SAME format as the Spring backend (without the "envelope") ----
  if (address === '/auth/me') {
    return { id: 'demo', username: status.user.name, email: 'demo@esempio.it', role: 'ROLE_USER', selectedAvatarId: null }
  }
  if (address === '/private/credits/balance') return { balance: status.user.credits }

  // Decks: list (without totals) and detail of every deck
  if (address === '/private/decks') {
    return decks.map((m) => ({ id: m.id, name: m.name, status: 'READY', factions: factionList(m) }))
  }
  if (pieces[0] === 'private' && pieces[1] === 'decks' && pieces[2]) {
    const m = decks.find((x) => x.id === Number(pieces[2]))
    if (m) return { id: m.id, name: m.name, status: 'READY', totalCards: m.cardCount, totalCost: m.points, factions: factionList(m), cards: [] }
  }

  if (request === 'GET /dashboard') {
    return {
      newRequests: 0,
      legendary: { packs: 7, total: 10 },
      recentMatches: finishedOnes()
        .slice(0, 3)
        .map((p) => ({ id: p.id, outcome: p.outcome, opponent: p.opponent.name, round: p.round })),
      trials: { completedCount: 3, totals: 12, highlighted: [{ id: 1, title: 'Vinci 10 partite', reward: '20 crediti', progress: 4, total: 10 }] },
    }
  }

  if (request === 'POST /matches') return { id: startMatch(body?.deckId).id }

  if (request === 'GET /matches' && pieces.length === 1) {
    // History: /matches?outcome=victory
    const outcome = params.get('outcome')
    return finishedOnes()
      .filter((p) => !outcome || p.outcome === outcome)
      .map((p) => ({ id: p.id, outcome: p.outcome, opponent: p.opponent.name, round: p.round }))
  }

  if (pieces[0] === 'matches') {
    const match = status.matches.find((p) => p.id === Number(pieces[1]))
    if (match && method === 'GET' && pieces[2] === 'replay') return match.replay
    if (match && method === 'GET' && !pieces[2]) return readMatch(match)
  }

  // Everything else does not exist in demo → same error as a real 404
  const error = new Error(`[demo] ${method} ${path} non esiste in modalità demo`)
  error.kind = 'request'
  error.status = 404
  throw error
}
