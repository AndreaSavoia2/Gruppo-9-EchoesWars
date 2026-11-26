import { apiGet, apiPost } from './client'

// ⚠️ TO BE CONFIRMED with the backend: paths and data shape.

/**
 * Echo Trials.
 * Response: { "completedCount": 3, "totals": 12, "creditsGained": 30, "totalCredits": 125,
 *             "trials": [ { "id": 1, "title": "Vinci la tua prima partita", "reward": "10 crediti",
 *                           "progress": 1, "total": 1, "claimed": true } ] }
 * Status of a trial: progress < total → in progress · progress = total and !claimed → Claim · claimed → ✓ Claimed
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getTrials(signal) {
  return apiGet('/trials', signal)
}

/**
 * Claims the reward of a completed trial.
 * Response: { "creditsAdded": 10 }
 *
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function claimTrial(id) {
  return apiPost(`/trials/${id}/claim`)
}

/**
 * Match history. Example: /matches?outcome=victory   (no outcome = all)
 * Response: [ { "id": 42, "outcome": "victory", "opponent": "@lyra_eco", "round": 14 } ]   outcome: victory | defeat | draw
 *
 * @param {string|null} outcome
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getMatchHistory(outcome, signal) {
  return apiGet(`/matches${outcome ? `?outcome=${outcome}` : ''}`, signal)
}

/**
 * Consecutive logins.
 * Response: { "day": 12, "total": 30,
 *             "rewards": [ { "day": 5, "name": "Avatar", "unlocked": true }, { "day": 15, "name": "Cover per carte", "unlocked": false } ] }
 *
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export function getLogins(signal) {
  return apiGet('/logins', signal)
}
