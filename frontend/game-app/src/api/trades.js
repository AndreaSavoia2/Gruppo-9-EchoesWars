import { apiGet, apiPost } from './client'

// ⚠️ TO BE CONFIRMED with the backend: paths and data shape.
//
// A "card" in trades:  { "id": 1, "name": "Velo Oscuro", "image": "https://...", "rarity": "Rara" }
// A "proposal":
// {
//   "id": 7,
//   "friend": { "id": 3, "name": "@ombra_sette" },
//   "offered":   card,   // the card given by WHOEVER MADE the proposal
//   "request":   card,   // the card asked in return
//   "expiresOn": "2026-11-23T18:00:00Z",
//   "status": "pending"   // pending | accepted | declined | expired | cancelled
// }

/**
 * Everything the Trades page needs.
 * Expected response:
 * {
 *   "isLocked": { "matches": 7, "requests": 10 },                       // null if the market is open
 *   "paused": { "endsOn": "2026-11-24T21:00:00Z", "withFriend": "@kael" },  // null if not paused
 *   "received": [proposal], "sent": [proposal], "history": [proposal]
 * }
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getTrades(signal) {
  return apiGet('/trades', signal)
}

/**
 * One proposal (for the "Conferma scambio" page)
 *
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getTrade(id, signal) {
  return apiGet(`/trades/${id}`, signal)
}

/**
 * Friends you can trade with. Response: [ { "id": 3, "name": "@lyra_eco", "friendsSince": "2026-11-03T00:00:00Z" } ]
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getTradeFriends(signal) {
  return apiGet('/trades/friends', signal)
}

/**
 * YOUR cards that you can offer.
 * Response: [ { ...card, "locked": null } ]   locked = null | "deck" | "reserved"
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getMyTradeCards(signal) {
  return apiGet('/trades/my-cards', signal)
}

/**
 * The friend's cards with the same rarity. Example: /trades/friends/3/cards?rarity=Rara
 *
 * @param {string|number} friendId
 * @param {string} rarity
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getFriendCards(friendId, rarity, signal) {
  return apiGet(`/trades/friends/${friendId}/cards?rarity=${encodeURIComponent(rarity)}`, signal)
}

/**
 * Sends a trade proposal.
 * Sends: { "friendId": 3, "offeredCardId": 1, "requestedCardId": 9 }  ·  Response: proposal
 *
 * @param {object} data
 * @returns {Promise<object>}
 */
export function sendProposal(data) {
  return apiPost('/trades', data)
}

/**
 * Accepts a received trade proposal.
 *
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function acceptTrade(id) {
  return apiPost(`/trades/${id}/accept`)
}
/**
 * Declines a received trade proposal.
 *
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function declineTrade(id) {
  return apiPost(`/trades/${id}/decline`)
}
/**
 * Cancels a trade proposal you sent.
 *
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function cancelProposal(id) {
  return apiPost(`/trades/${id}/cancel`)
}
