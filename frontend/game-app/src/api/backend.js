import { apiGet } from './client'

// 🔁 "Translators" from the Spring backend format to the format used by the pages.
// This way, if the backend renames a field, it only needs fixing HERE.

/** Rarity by level (backend "tiers" table: 1 Common, 2 Rare, 3 Legendary). */
export const RARITY_BY_LEVEL = { 1: 'Comune', 2: 'Rara', 3: 'Leggendaria' }

/** Echo ids in the backend ("factions" table). Needed to create a deck. */
export const ECHO_IDS = { Dominio: 1, Sapienza: 2, Oblio: 3, Inganno: 4 }

/** Italian names used by the backend (needed for the filters). */
export const ECHO_NAMES = ['Dominio', 'Sapienza', 'Oblio', 'Inganno']
export const SCHOOL_NAMES = ['Guerriero', 'Mago', 'Ladro']
export const RARITY_NAMES = ['Comune', 'Rara', 'Leggendaria']

/**
 * Turns a backend card (CardResponse) into the page format.
 * @param {object} c - card as it comes from the backend
 * @returns {object} card ready for <GameCard> and for the pages
 */
export function cardFromBackend(c) {
  return {
    id: c.id,
    code: c.code,
    name: c.nameIt,
    description: c.descriptionIt,
    atk: c.attack,
    def: c.defense,
    cost: c.cost,
    points: c.cost, // in a deck, "points" are the sum of the costs (max 98)
    echo: c.echoesNameIt,
    school: c.schoolNameIt,
    rarity: RARITY_BY_LEVEL[c.tierLevel] ?? 'Comune',
    amount: c.quantity ?? 0,
    // If the backend has no image yet, use the local copy named after the card code (e.g. DOM01.webp)
    illustration: c.artUrl || (c.code ? `/images/illustrations/${c.code}.webp` : null),
    frame: c.frameImageUrl || null, // if missing, <GameCard> uses the local frames
    // ⚠️ The backend does not send the card effect yet (e.g. "PIERCING")
    effect: null,
    ability: null,
  }
}

/**
 * Downloads ALL the pages of a paginated backend list and joins them.
 * The backend answers { content, totalPages, totalElements, currentPage }
 * and gives at most 10 items per page.
 * @param {string} path - e.g. "/private/collections"
 * @param {AbortSignal} signal - to cancel if the page is closed
 * @param {string} [order] - sorting parameters, e.g. "sortBy=card.id&direction=ASC"
 * @returns {Promise<{ items: object[], total: number }>}
 */
export async function allPages(path, signal, order = '') {
  const items = []
  let page = 0
  let pageCount = 1
  let total = 0
  // At most 100 pages (= 1000 items): a safety brake
  while (page < pageCount && page < 100) {
    const data = await apiGet(`${path}?pageNumber=${page}&pageSize=10${order ? `&${order}` : ''}`, signal)
    items.push(...(data?.content ?? []))
    pageCount = data?.totalPages ?? 0
    total = data?.totalElements ?? items.length
    page++
  }
  return { items, total }
}
