import { apiGet, apiPost } from './client'
import { RARITY_BY_LEVEL, cardFromBackend } from './backend'

/**
 * Packs to open and progress towards the guaranteed legendary.
 * ✅ CONNECTED:
 *   GET /private/packs/purchases/unopened                       → [{ purchaseId, ... }]
 *   GET /private/packs/purchases/{id}/legendary-guarantee-progress
 *       → { cycleLength, completedOpeningsInCurrentCycle, openingsUntilGuaranteedLegendary, ... }
 * @param {AbortSignal} signal
 * @returns {Promise<{ availableItems: number, purchases: object[], legendary: { packs: number, total: number } }>}
 */
export async function getPacks(signal) {
  const purchases = (await apiGet('/private/packs/purchases/unopened', signal)) ?? []
  let legendary = { packs: 0, total: 10 }
  if (purchases.length > 0) {
    try {
      const p = await apiGet(`/private/packs/purchases/${purchases[0].purchaseId}/legendary-guarantee-progress`, signal)
      legendary = { packs: p.completedOpeningsInCurrentCycle, total: p.cycleLength }
    } catch (error) {
      if (error.name === 'AbortError') throw error
      // if the progress does not arrive, we still show the packs
    }
  }
  return { availableItems: purchases.length, purchases, legendary }
}

/**
 * Opens one or more packs (one at a time, starting from the oldest).
 * ✅ CONNECTED: POST /private/packs/purchases/{purchaseId}/open → { cards: [CardResponse], ... }
 * @param {number} amount - how many packs to open
 * @returns {Promise<{ cardList: object[], summary: string }>}
 */
export async function openPacks(amount) {
  const { purchases } = await getPacks()
  const toOpen = purchases.slice(0, amount)
  const cardList = []
  for (const purchase of toOpen) {
    const opening = await apiPost(`/private/packs/purchases/${purchase.purchaseId}/open`)
    cardList.push(...(opening?.cards ?? []).map(cardFromBackend))
  }
  return { cardList, summary: summary(cardList) }
}

/**
 * Sentence like "3 comuni, 1 rara e 1 leggendaria." (3 common, 1 rare and 1 legendary)
 * @param {object[]} cardList
 * @returns {string}
 */
function summary(cardList) {
  const names = { Comune: ['comune', 'comuni'], Rara: ['rara', 'rare'], Leggendaria: ['leggendaria', 'leggendarie'] }
  const parts = Object.values(RARITY_BY_LEVEL)
    .map((r) => [r, cardList.filter((c) => c.rarity === r).length])
    .filter(([, n]) => n > 0)
    .map(([r, n]) => `${n} ${names[r][n === 1 ? 0 : 1]}`)
  if (parts.length === 0) return ''
  return parts.length === 1 ? `${parts[0]}.` : `${parts.slice(0, -1).join(', ')} e ${parts.at(-1)}.`
}
