import { cancellaToken } from './token'
import { linguaScelta } from '../utils/lingua'
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
    lingua: linguaScelta().nome, // ⚠️ salvata solo nel browser: il backend non ha ancora la lingua
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

/**
 * Cambia la password.
 * ✅ COLLEGATO: POST /auth/change-password { currentPassword, newPassword }
 * Dopo il cambio il backend chiude TUTTE le sessioni: bisogna rifare l'accesso.
 * Errori: 401 = password attuale sbagliata · 400 = nuova password non valida
 *         (meno di 12 caratteri, oppure uguale a quella attuale)
 * @param {string} attuale - la password di adesso
 * @param {string} nuova - la nuova password (almeno 12 caratteri)
 * @returns {Promise<null>}
 */
export function cambiaPassword(attuale, nuova) {
  return apiPost('/auth/change-password', { currentPassword: attuale, newPassword: nuova })
}
