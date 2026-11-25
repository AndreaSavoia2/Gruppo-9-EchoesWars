import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

/**
 * Lista degli amici da sfidare.
 * Risposta: [ { "id": 7, "nome": "@nome", "ultimaSfida": "2026-11-21T18:00:00Z" } ]  (ultimaSfida = null se mai sfidato)
 *
 * @param {AbortSignal} signal
 */
export function getAmici(signal) {
  return apiGet('/amici', signal)
}

/**
 * Invia la richiesta di sfida a un amico.
 * Invia: { "amicoId": 7 }   ·   Risposta: { "id": 15 }  ← id della sfida
 *
 * @param {*} amicoId
 */
export function inviaSfida(amicoId) {
  return apiPost('/sfide', { amicoId })
}

/**
 * Sceglie il mazzo per la sfida.   Invia: { "mazzoId": 1 }
 *
 * @param {*} sfidaId
 * @param {*} mazzoId
 */
export function scegliMazzoSfida(sfidaId, mazzoId) {
  return apiPost(`/sfide/${sfidaId}/mazzo`, { mazzoId })
}

/**
 * Annulla la richiesta di sfida.
 *
 * @param {*} sfidaId
 */
export function annullaSfida(sfidaId) {
  return apiPost(`/sfide/${sfidaId}/annulla`)
}

/**
 * Stato della sfida.
 * Risposta attesa:
 * {
 *   "id": 15,
 *   "stato": "in_attesa",            // "in_attesa" | "accettata" | "rifiutata" | "scaduta" | "annullata"
 *   "amico": { "id": 7, "nome": "@nome" },
 *   "mioMazzo": "Nome del mio mazzo",
 *   "mazzoAmico": "Nome mazzo dell'amico",  // quando ha accettato
 *   "scadeIl": "2026-11-22T21:31:00Z",      // quando scade la richiesta
 *   "partitaId": 42                         // quando ha accettato: la partita da guardare
 * }
 *
 * @param {string|number} id
 * @param {AbortSignal} signal
 */
export function getSfida(id, signal) {
  return apiGet(`/sfide/${id}`, signal)
}
