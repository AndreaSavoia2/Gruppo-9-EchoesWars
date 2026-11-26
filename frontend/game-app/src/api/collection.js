import { apiPost } from './client'
import { ECHO_NAMES, RARITY_NAMES, SCHOOL_NAMES, cardFromBackend, allPages } from './backend'

/**
 * All the owned cards, already in the page format.
 * ✅ CONNECTED: GET /private/collections (10 per page: we download them all)
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export async function getAllCards(signal) {
  const { items } = await allPages('/private/collections', signal, 'sortBy=card.id&direction=ASC')
  return items.map(cardFromBackend)
}

/**
 * The collection cards with the filters applied.
 * ⚠️ The backend has no filters yet: we apply them here in the browser.
 * @param {{ school: string[], echo: string[], rarity: string[], ability: string[] }} filters
 * @param {AbortSignal} signal
 * @returns {Promise<{ total: number, foundCards: number, cardList: object[] }>}
 */
export async function getCollection(filters, signal) {
  const all = await getAllCards(signal)
  // An empty filter = "everything". Otherwise the card value must be among the chosen ones.
  const pass = (values, value) => values.length === 0 || values.includes(value)
  const cardList = all.filter(
    (card) =>
      pass(filters.school, card.school) &&
      pass(filters.echo, card.echo) &&
      pass(filters.rarity, card.rarity) &&
      pass(filters.ability, card.ability),
  )
  return { total: all.length, foundCards: cardList.length, cardList }
}

/**
 * The possible values for each filter (they are fixed, as in the backend database).
 * ⚠️ "ability" is empty: the backend does not send the card effects yet.
 * @returns {Promise<{ school: string[], echo: string[], rarity: string[], ability: string[] }>}
 */
export async function getCollectionFilters() {
  return { school: SCHOOL_NAMES, echo: ECHO_NAMES, rarity: RARITY_NAMES, ability: [] }
}

/**
 * All the data of ONE collection card.
 * ⚠️ The backend has no "GET card by id" yet: we look for it in the collection.
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export async function getCard(id, signal) {
  const card = (await getAllCards(signal)).find((c) => String(c.id) === String(id))
  if (!card) {
    const error = new Error(`Carta ${id} non trovata nella collezione`)
    error.kind = 'request'
    error.status = 404
    throw error
  }
  return {
    ...card,
    // ⚠️ STILL MISSING IN THE BACKEND: copies in decks and conversion of duplicates
    copiesInDecks: 0,
    conversion: null,
  }
}

/**
 * ⚠️ NOT IN THE BACKEND YET: converts duplicates into credits.
 * Expected response: { "creditsAdded": 10, "amount": 2 }
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function convertCopies(id) {
  return apiPost(`/collection/cards/${id}/convert`)
}
