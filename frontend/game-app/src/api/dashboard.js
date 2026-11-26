import { apiGet } from './client'
import { getPacks } from './packs'
import { getRequests } from './requests'

/**
 * The Home data. ⚠️ GET /dashboard is NOT in the backend yet:
 * if it does not answer, the data is taken from the other endpoints (see below).
 *
 * Expected response:
 * {
 *   "newRequests": 2,                          // number of unread requests
 *   "legendary": { "packs": 7, "total": 10 },
 *   "recentMatches": [
 *     { "id": 1, "outcome": "victory", "opponent": "@name", "round": 14 }
 *   ],                                         // outcome: "victory" or "defeat"
 *   "trials": {
 *     "completedCount": 3,
 *     "totals": 12,
 *     "highlighted": [
 *       { "id": 1, "title": "...", "reward": "10 crediti", "progress": 0, "total": 1 }
 *     ]
 *   }
 * }
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export async function getDashboard(signal) {
  try {
    return await apiGet('/dashboard', signal)
  } catch (error) {
    if (error.name === 'AbortError') throw error
    return dashboardFromOtherEndpoints(signal)
  }
}

/**
 * ⚠️ The backend has no /dashboard yet: we build the Home with the endpoints that exist.
 *   ✅ new requests          ← GET /private/friendships/requests/received
 *   ✅ guaranteed legendary  ← GET /private/packs/purchases/unopened (+ progress)
 *   ⏳ recent matches and Echo Trials: null → the Home shows "Presto disponibile" (coming soon)
 * If these endpoints do not answer either, the Home still shows the greeting and PLAY.
 * @param {AbortSignal} signal
 * @returns {Promise<{ newRequests: number, legendary: object|null, recentMatches: null, trials: null }>}
 */
async function dashboardFromOtherEndpoints(signal) {
  const [requests, packs] = await Promise.allSettled([getRequests(signal), getPacks(signal)])
  return {
    newRequests: requests.status === 'fulfilled' ? requests.value.length : 0,
    legendary: packs.status === 'fulfilled' ? packs.value.legendary : null,
    recentMatches: null,
    trials: null,
  }
}
