import { apiGet, apiPost } from './client'

// ⚠️ TO BE CONFIRMED with the backend: paths and data shape.

/**
 * Starts a random match with the chosen deck.
 * Sends:    { "mode": "random", "deckId": 1 }
 * Response: { "id": 42 }   ← the id of the match just created
 *
 * @param {string|number} deckId
 * @returns {Promise<{ id: number }>}
 */
export function startRandomMatch(deckId) {
  return apiPost('/matches', { mode: 'random', deckId })
}

/**
 * Status and result of a match.
 * Expected response:
 * {
 *   "id": 42,
 *   "status": "in_progress",          // "in_progress", "finished" or "interrupted"
 *                                     // (interrupted = the connection dropped: the system ends it by itself)
 *   "myDeck": "Name of my deck",
 *   "opponent": { "name": "@name" },
 *   // these only arrive when status = "finished":
 *   "outcome": "victory",             // "victory" or "defeat"
 *   "myHp": 6, "opponentHp": 0,       // final hit points
 *   "creditsEarned": 10,
 *   "round": 14,
 *   // for the Details page:
 *   "playedOn": "2026-11-22T21:30:00Z",   // date and time of the match
 *   "roundMax": 100,
 *   "opponent": { "name": "@name", "deck": "Opponent deck name" },
 *   "myCardsLeft": 19, "opponentCardsLeft": 17,
 *   "endReason": "PV a 0"                  // how it ended (shown to the user)
 * }
 *
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getMatch(id, signal) {
  return apiGet(`/matches/${id}`, signal)
}

/**
 * Animated battle of the match, round by round.
 * Used both right after matchmaking (/match/:id/battle) and for "Rivedi partita" (watch again).
 * Every round has 3 moments: cards on the field → clash → outcome.
 * Expected response:
 * {
 *   "roundMax": 100,
 *   "opponent": { "name": "@name" },
 *   "round": [
 *     {
 *       "count": 3,
 *       // situation at the START of the round (cards on the field)
 *       "you":      { "hp": 10, "deckCount": 25, "discardCount": 2,
 *                     "lastDiscarded": "https://.../card.png",    // image, or null
 *                     "card": { "name": "Card name", "image": "https://...", "def": 2 } },
 *       "opponent": { ...like "you"... },
 *       // damage taken during the clash (0 = none)
 *       "yourDamage": 3, "opponentDamage": 5,
 *       // end of round message (or null)
 *       "outcome": { "title": "La tua carta è distrutta", "detail": "Danno in eccesso: perdi 1 PV" },
 *       // situation at the END of the round ("card": null if it was destroyed)
 *       "after": { "you": { ... }, "opponent": { ... } }
 *     }
 *   ]
 * }
 *
 * @param {string|number} id
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getReplay(id, signal) {
  return apiGet(`/matches/${id}/replay`, signal)
}
