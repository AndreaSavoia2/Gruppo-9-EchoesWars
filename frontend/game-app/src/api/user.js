import { apiGet, apiPost } from './client'

/**
 * ✅ CONNECTED to the backend: the user data for the top bar.
 * It combines two calls:
 *   GET /auth/me                 → { id, username, email, role, selectedAvatarId, ... }
 *   GET /private/credits/balance → { balance }
 * Returns to the pages:
 *   { name, email, credits, avatarId, avatar, firstLogin }
 * @param {AbortSignal} signal
 * @returns {Promise<object>}
 */
export async function getUser(signal) {
  const [me, balanceValue] = await Promise.all([
    apiGet('/auth/me', signal),
    // If the credit wallet does not exist yet, we show 0 instead of blocking everything
    apiGet('/private/credits/balance', signal).catch((error) => {
      if (error.name === 'AbortError') throw error
      return { balance: 0 }
    }),
  ])
  return {
    name: me.username,
    email: me.email,
    credits: balanceValue?.balance ?? 0,
    avatarId: me.selectedAvatarId ?? null,
    // ⚠️ STILL MISSING IN THE BACKEND: the avatar image (there is only the id) → default icon
    avatar: null,
    // ⚠️ STILL MISSING IN THE BACKEND: "first login" → for now the welcome does not start by itself
    firstLogin: false,
  }
}

/**
 * ⚠️ NOT IN THE BACKEND YET: marks that the user has seen the welcome screens.
 * Sends: nothing   Response: nothing (204)
 * @returns {Promise<null>}
 */
export function markWelcomeSeen() {
  return apiPost('/user/welcome')
}
