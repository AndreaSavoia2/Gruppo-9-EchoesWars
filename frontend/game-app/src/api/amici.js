import { apiDelete, apiGet, apiPost } from './client'

/**
 * ⚠️ NON ANCORA NEL BACKEND: la lista dei tuoi amici.
 * Risposta attesa: [ { "id": 3, "nome": "@lyra_eco" } ]
 */
export function getAmiciLista(signal) {
  return apiGet('/amici', signal)
}

/**
 * Manda una richiesta di amicizia.
 * ✅ COLLEGATO: POST /private/friendships/requests/{username}
 * Se il nome non esiste il backend risponde con un errore.
 * @param {string} nome - es. "@aurora_eco" (la chiocciola viene tolta)
 */
export function aggiungiAmico(nome) {
  const username = nome.replace(/^@/, '')
  return apiPost(`/private/friendships/requests/${encodeURIComponent(username)}`)
}

/**
 * Cerca utenti per nome (per suggerimenti mentre si scrive).
 * ✅ COLLEGATO: GET /private/users?username=... → [{ id, username }]
 * @param {string} testo
 * @param {AbortSignal} signal
 * @returns {Promise<{ id: string, nome: string }[]>}
 */
export async function cercaUtenti(testo, signal) {
  const utenti = await apiGet(`/private/users?username=${encodeURIComponent(testo.replace(/^@/, ''))}`, signal)
  return (utenti ?? []).map((u) => ({ id: u.id, nome: `@${u.username}` }))
}

/** ⚠️ NON ANCORA NEL BACKEND: elimina un amico. */
export function eliminaAmico(id) {
  return apiDelete(`/amici/${id}`)
}
