import { apiDelete, apiGet, apiPost } from './client'

/**
 * ⚠️ NOT IN THE BACKEND YET: the list of your friends.
 * Expected response: [ { "id": 3, "name": "@lyra_eco" } ]
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export function getFriendList(signal) {
  return apiGet('/friends', signal)
}

/**
 * Sends a friend request.
 * ✅ CONNECTED: POST /private/friendships/requests/{username}
 * If the name does not exist the backend answers with an error.
 * @param {string} name - e.g. "@aurora_eco" (the @ is removed)
 * @returns {Promise<object>}
 */
export function addFriend(name) {
  const username = name.replace(/^@/, '')
  return apiPost(`/private/friendships/requests/${encodeURIComponent(username)}`)
}

/**
 * Searches users by name (for suggestions while typing).
 * ✅ CONNECTED: GET /private/users?username=... → [{ id, username }]
 * @param {string} text
 * @param {AbortSignal} signal
 * @returns {Promise<{ id: string, name: string }[]>}
 */
export async function searchUsers(text, signal) {
  const users = await apiGet(`/private/users?username=${encodeURIComponent(text.replace(/^@/, ''))}`, signal)
  return (users ?? []).map((u) => ({ id: u.id, name: `@${u.username}` }))
}

/**
 * ⚠️ NOT IN THE BACKEND YET: removes a friend.
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export function removeFriend(id) {
  return apiDelete(`/friends/${id}`)
}
