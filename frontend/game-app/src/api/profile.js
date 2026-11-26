import { clearToken } from './token'
import { chosenLanguage } from '../utils/language'
import { apiGet, apiPost, apiPut } from './client'

/**
 * Profile data.
 * ✅ CONNECTED (name and email): GET /auth/me → { username, email, ... }
 * ⚠️ STILL MISSING IN THE BACKEND: matches, wins, cards, language and consecutive logins
 *    (for now we show "–")
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export async function getProfile(signal) {
  const me = await apiGet('/auth/me', signal)
  return {
    name: `@${me.username}`,
    email: me.email,
    matches: '–',
    wins: '–',
    cardCount: '–',
    language: chosenLanguage().name, // ⚠️ saved only in the browser: the backend has no language yet
    logins: null,
  }
}

/**
 * ⚠️ NOT IN THE BACKEND YET: the list of avatars.
 * Expected response: [ { "id": 3, "name": "Libro", "image": "https://...",
 *                        "available": true, "unlock": null, "inUse": true } ]
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getAvatar(signal) {
  return apiGet('/profile/avatar', signal)
}

/**
 * Chooses the avatar.
 * ✅ CONNECTED: PUT /auth/me/selected-avatar { avatarId }
 * ⚠️ Today the backend CORS only allows GET and POST: until it adds PUT,
 *    the browser blocks this request.
 * @param {number} avatarId
 * @returns {Promise<object>}
 */
export function saveAvatar(avatarId) {
  return apiPut('/auth/me/selected-avatar', { avatarId })
}

/**
 * Logs out of the account.
 * ✅ CONNECTED: POST /auth/logout → the backend closes the session and deletes the refresh cookie.
 * Then we "throw away the wristband": we delete the token from the browser.
 * Even if the backend does not answer, the token is deleted anyway.
 * @returns {Promise<void>}
 */
export async function exit() {
  try {
    await apiPost('/auth/logout')
  } catch {
    // nothing to do: we log out anyway
  } finally {
    clearToken()
  }
}

/**
 * Changes the password.
 * ✅ CONNECTED: POST /auth/change-password { currentPassword, newPassword }
 * After the change the backend closes ALL the sessions: the user has to log in again.
 * Errors: 401 = wrong current password · 400 = new password not valid
 *         (fewer than 12 characters, or the same as the current one)
 * @param {string} current - the password in use now
 * @param {string} newPassword - the new password (at least 12 characters)
 * @returns {Promise<null>}
 */
export function changePassword(current, newPassword) {
  return apiPost('/auth/change-password', { currentPassword: current, newPassword })
}
