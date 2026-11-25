import { apiGet, apiPost } from './client'

/**
 * ✅ COLLEGATO al backend: i dati dell'utente per la barra in alto.
 * Unisce due chiamate:
 *   GET /auth/me                 → { id, username, email, role, selectedAvatarId, ... }
 *   GET /private/credits/balance → { balance }
 * Restituisce alle pagine:
 *   { nome, email, crediti, avatarId, avatar, primoAccesso }
 * @param {AbortSignal} signal
 */
export async function getUtente(signal) {
  const [io, saldo] = await Promise.all([
    apiGet('/auth/me', signal),
    // Se il portafoglio crediti non esiste ancora, mostriamo 0 invece di bloccare tutto
    apiGet('/private/credits/balance', signal).catch((errore) => {
      if (errore.name === 'AbortError') throw errore
      return { balance: 0 }
    }),
  ])
  return {
    nome: io.username,
    email: io.email,
    crediti: saldo?.balance ?? 0,
    avatarId: io.selectedAvatarId ?? null,
    // ⚠️ DA FARE NEL BACKEND: manca l'immagine dell'avatar (c'è solo l'id) → icona standard
    avatar: null,
    // ⚠️ DA FARE NEL BACKEND: manca "primo accesso" → per ora il benvenuto non parte da solo
    primoAccesso: false,
  }
}

/**
 * ⚠️ NON ANCORA NEL BACKEND: segna che l'utente ha visto il benvenuto.
 * Invia: niente   Risposta: niente (204)
 */
export function segnaBenvenutoVisto() {
  return apiPost('/utente/benvenuto')
}
