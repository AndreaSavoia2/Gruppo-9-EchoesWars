import { apiGet, apiPost, apiPut } from './client'
import { ECHO_IDS, ECHO_NAMES, SCHOOL_NAMES, cardFromBackend } from './backend'
import { getAllCards } from './collection'
import { DECK_RULES } from '../data/rules'

// ⚠️ TO BE CONFIRMED with the backend: the maximum number of decks you can have
const MAX_DECKS = 20

/**
 * The detail of a deck, exactly as it comes from the backend.
 * ✅ CONNECTED: GET /private/decks/{id}
 *   → { id, name, status, totalCards, totalCost, factions: [{ id, nameIt }], cards: [CardResponse] }
 *   (in "cards", quantity = copies of that card IN THE DECK)
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
function getDetail(id, signal) {
  return apiGet(`/private/decks/${id}`, signal)
}

/**
 * Turns the detail of a deck into the deck list format.
 * A deck is "valid" if it has exactly 30 cards and at most 98 points.
 * @param {object} d - deck detail from the backend
 * @returns {object}
 */
function deckFromBackend(d) {
  const reasons = []
  if (d.totalCards !== DECK_RULES.cards) reasons.push(`${d.totalCards}/${DECK_RULES.cards} carte`)
  if (d.totalCost > DECK_RULES.points) reasons.push(`${d.totalCost}/${DECK_RULES.points} punti`)
  return {
    id: d.id,
    name: d.name,
    valid: reasons.length === 0,
    reason: reasons[0] ?? null,
    cardCount: d.totalCards,
    maxCards: DECK_RULES.cards,
    points: d.totalCost,
    maxPoints: DECK_RULES.points,
    schools: (d.factions ?? []).map((f) => f.nameIt), // the 2 Echoes of the deck
    cardSchools: [...new Set((d.cards ?? []).map((c) => c.schoolNameIt))], // for the "School" filter
  }
}

/**
 * The player's decks, with filters and sorting (applied here in the browser).
 * ✅ CONNECTED: GET /private/decks?status=READY + the detail of every deck
 *   (the backend list has no cards and points, the detail does)
 * @param {AbortSignal} signal
 * @param {{ school?: string[], echo?: string[], sort?: string[] }} [filters]
 * @returns {Promise<{ total: number, maximum: number, decks: object[] }>}
 */
export async function getDecks(signal, filters = {}) {
  const list = (await apiGet('/private/decks?status=READY', signal)) ?? []
  const details = await Promise.all(list.map((m) => getDetail(m.id, signal)))
  const everyone = details.map(deckFromBackend)

  const school = filters.school ?? []
  const echo = filters.echo ?? []
  let decks = everyone.filter(
    (m) =>
      (school.length === 0 || school.some((s) => m.cardSchools.includes(s))) &&
      (echo.length === 0 || echo.some((e) => m.schools.includes(e))),
  )

  const sort = [].concat(filters.sort ?? [])[0]
  if (sort === 'Nome (A-Z)') decks = [...decks].sort((a, b) => a.name.localeCompare(b.name, 'it'))
  else if (sort === 'Punti') decks = [...decks].sort((a, b) => b.points - a.points)
  else decks = [...decks].sort((a, b) => b.id - a.id) // "Most recent" (higher id = newer)

  return { total: everyone.length, maximum: MAX_DECKS, decks }
}

/**
 * The possible values of the deck list filters (fixed).
 * @returns {Promise<{ school: string[], echo: string[], sort: string[] }>}
 */
export async function getDeckFilters() {
  return { school: SCHOOL_NAMES, echo: ECHO_NAMES, sort: ['Più recenti', 'Nome (A-Z)', 'Punti'] }
}

/**
 * A deck to edit.
 * ✅ CONNECTED: GET /private/decks/{id}
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<{ id: number, name: string, echoes: string[], cardList: { cardId: number, copies: number }[] }>}
 */
export async function getDeck(id, signal) {
  const d = await getDetail(id, signal)
  return {
    id: d.id,
    name: d.name,
    echoes: (d.factions ?? []).map((f) => f.nameIt),
    cardList: (d.cards ?? []).map((c) => ({ cardId: c.id, copies: c.quantity ?? 1, card: cardFromBackend(c) })),
  }
}

/**
 * The cards you OWN from the 2 chosen Echoes (to build the deck).
 * ✅ CONNECTED: uses the collection (GET /private/collections) and filters by Echo.
 * @param {string[]} echoes - e.g. ["Sapienza", "Oblio"]
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export async function getAvailableCards(echoes, signal) {
  const all = await getAllCards(signal)
  return all.filter((card) => echoes.includes(card.echo))
}

/**
 * Saves the deck: new (POST) or edited (PUT).
 * ✅ CONNECTED:
 *   POST /private/decks      { name, factionIds: [1, 3], cards: [{ cardId, quantity }] }
 *   PUT  /private/decks/{id} { name, cards: [{ cardId, quantity }] }
 * ⚠️ Today the backend CORS only allows GET and POST: EDITING (PUT) will be blocked
 *    until the backend adds PUT and DELETE.
 * @param {{ name: string, echoes: string[], cardList: { cardId: number, copies: number }[] }} deck
 * @param {string|number} [id] - if present, edits that deck
 * @returns {Promise<{ id: number }>}
 */
export async function saveDeck(deck, id) {
  const cards = deck.cardList.map((c) => ({ cardId: c.cardId, quantity: c.copies }))
  const saved = id
    ? await apiPut(`/private/decks/${id}`, { name: deck.name, cards })
    : await apiPost('/private/decks', { name: deck.name, factionIds: deck.echoes.map((e) => ECHO_IDS[e]), cards })
  return { id: saved?.id }
}
