import { cancellaToken } from './token'
import { apiGet, apiPost, apiPut } from './client'

/**
 * Dati del profilo.
 * ✅ COLLEGATO (nome ed email): GET /auth/me → { username, email, ... }
 * ⚠️ DA FARE NEL BACKEND: partite, vittorie, carte, lingua e accessi consecutivi
 *    (per ora mostriamo "–")
 * @param {AbortSignal} signal
 */
export async function getProfilo(signal) {
  const io = await apiGet('/auth/me', signal)
  return {
    nome: `@${io.username}`,
    email: io.email,
    partite: '–',
    vittorie: '–',
    carte: '–',
    lingua: 'Italiano',
    accessi: null,
  }
}

/**
 * ⚠️ NON ANCORA NEL BACKEND: la lista degli avatar.
 * Risposta attesa: [ { "id": 3, "nome": "Libro", "immagine": "https://...",
 *                      "disponibile": true, "sblocco": null, "inUso": true } ]
 */
export function getAvatar(signal) {
  return apiGet('/profilo/avatar', signal)
}

/**
 * Sceglie l'avatar.
 * ✅ COLLEGATO: PUT /auth/me/selected-avatar { avatarId }
 * ⚠️ Il CORS del backend oggi permette solo GET e POST: finché non aggiunge PUT,
 *    il browser blocca questa richiesta.
 * @param {number} avatarId
 */
export function salvaAvatar(avatarId) {
  return apiPut('/auth/me/selected-avatar', { avatarId })
}

/**
 * Esci dall'account.
 * ✅ COLLEGATO: POST /auth/logout → il backend chiude la sessione e cancella il cookie di rinnovo.
 * Poi "buttiamo via il braccialetto": cancelliamo il token dal browser.
 * Anche se il backend non risponde, il token viene cancellato lo stesso.
 */
export async function esci() {
  try {
    await apiPost('/auth/logout')
  } catch {
    // niente: usciamo comunque
  } finally {
    cancellaToken()
  }
}
