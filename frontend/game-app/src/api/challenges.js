import { apiGet, apiPost } from './client'

// ⚠️ TO BE CONFIRMED with the backend: paths and data shape.

/**
 * List of friends you can challenge.
 * Response: [ { "id": 7, "name": "@name", "lastChallenge": "2026-11-21T18:00:00Z" } ]  (lastChallenge = null if never challenged)
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getFriends(signal) {
  return apiGet('/friends', signal)
}

/**
 * Sends a challenge request to a friend.
 * Sends: { "friendId": 7 }   ·   Response: { "id": 15 }  ← id of the challenge
 *
 * @param {string|number} friendId
 * @returns {Promise<{ id: number }>}
 */
export function sendChallenge(friendId) {
  return apiPost('/challenges', { friendId })
}

/**
 * Chooses the deck for the challenge.   Sends: { "deckId": 1 }
 *
 * @param {string|number} challengeId
 * @param {string|number} deckId
 * @returns {Promise<object>}
 */
export function chooseChallengeDeck(challengeId, deckId) {
  return apiPost(`/challenges/${challengeId}/deck`, { deckId })
}

/**
 * Cancels the challenge request.
 *
 * @param {string|number} challengeId
 * @returns {Promise<object>}
 */
export function cancelChallenge(challengeId) {
  return apiPost(`/challenges/${challengeId}/cancel`)
}

/**
 * Status of the challenge.
 * Expected response:
 * {
 *   "id": 15,
 *   "status": "pending",            // "pending" | "accepted" | "declined" | "expired" | "cancelled"
 *   "friend": { "id": 7, "name": "@name" },
 *   "myDeck": "Name of my deck",
 *   "friendDeck": "Name of the friend's deck",  // once accepted
 *   "expiresOn": "2026-11-22T21:31:00Z",         // when the request expires
 *   "matchId": 42                                // once accepted: the match to watch
 * }
 *
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getChallenge(id, signal) {
  return apiGet(`/challenges/${id}`, signal)
}
