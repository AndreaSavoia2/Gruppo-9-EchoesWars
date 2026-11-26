import { apiGet, apiPost } from './client'

/**
 * Received requests.
 * ✅ CONNECTED (friendship only): GET /private/friendships/requests/received
 *   → [{ id, requesterUsername, status, requestedAt, ... }]
 * ⚠️ CHALLENGE requests are not in the backend yet.
 * Returns: [ { "id": 5, "kind": "friendship", "from": { "name": "@kael" }, "receivedOn": "2026-11-22T21:28:00" } ]
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export async function getRequests(signal) {
  const received = (await apiGet('/private/friendships/requests/received', signal)) ?? []
  return received
    .filter((r) => r.status === 'PENDING')
    .map((r) => ({
      id: r.id,
      kind: 'friendship',
      from: { name: `@${r.requesterUsername}` },
      receivedOn: r.requestedAt,
    }))
}

/**
 * ⚠️ NOT IN THE BACKEND YET: accepts a request.
 * For a CHALLENGE it also sends the deck: { "deckId": 1 } and answers { "matchId": 42 }
 * @param {string|number} id
 * @param {string|number} [deckId] - only for a challenge
 * @returns {Promise<object>}
 */
export function acceptRequest(id, deckId) {
  return apiPost(`/requests/${id}/accept`, deckId ? { deckId } : undefined)
}

/**
 * ⚠️ NOT IN THE BACKEND YET: declines a request.
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function declineRequest(id) {
  return apiPost(`/requests/${id}/decline`)
}
