import { apiGet, apiPost } from './client'
import { getPacks } from './packs'

// ⚠️ STILL MISSING IN THE BACKEND: the price is not in the pack list.
// In the database the "Pacchetto Base" (basic pack) costs 300 credits: we use it until the backend sends it.
const TEMPORARY_PRICE = 300

/**
 * The shop products, the packs to open and the progress towards the legendary.
 * ✅ CONNECTED:
 *   GET /private/packs                     → [{ id, code, nameIt, descriptionIt, ... }]
 *   GET /private/packs/purchases/unopened  → packs bought and not opened yet
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export async function getShop(signal) {
  const [bundles, packs] = await Promise.all([apiGet('/private/packs', signal), getPacks(signal)])
  return {
    products: (bundles ?? []).map((p) => ({
      id: p.id,
      name: p.nameIt,
      image: '/images/pack.png',
      description: p.descriptionIt,
      detail: null,
      price: p.priceCredits ?? TEMPORARY_PRICE,
    })),
    // ⚠️ Fixed text from the game rules (the backend does not send it)
    creditRules: 'Vittoria +10 · Pareggio +5 · Sconfitta +2',
    packsToOpen: packs.availableItems,
    legendary: packs.legendary,
  }
}

/**
 * Buys a pack.
 * ✅ CONNECTED: POST /private/packs/{id}/purchases → { purchaseId, remainingBalance, ... }
 * (the top bar reloads the new credits)
 * @param {number} productId - id of the pack
 * @returns {Promise<object>}
 */
export function buy(productId) {
  return apiPost(`/private/packs/${productId}/purchases`)
}
