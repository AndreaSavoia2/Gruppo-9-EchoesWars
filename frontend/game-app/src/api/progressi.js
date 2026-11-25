import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

/**
 * Prove degli Echi.
 * Risposta: { "completate": 3, "totali": 12, "creditiOttenuti": 30, "creditiTotali": 125,
 *             "prove": [ { "id": 1, "titolo": "Vinci la tua prima partita", "ricompensa": "10 crediti",
 *                          "fatto": 1, "totale": 1, "riscossa": true } ] }
 * Stato di una prova: fatto < totale → In corso · fatto = totale e !riscossa → Riscuoti · riscossa → ✓ Riscossa
 *
 * @param {AbortSignal} signal
 */
export function getProve(signal) {
  return apiGet('/prove', signal)
}

/**
 * Risposta: { "creditiAggiunti": 10 }
 *
 * @param {string|number} id
 */
export function riscuotiProva(id) {
  return apiPost(`/prove/${id}/riscuoti`)
}

/**
 * Cronologia. Esempio: /partite?esito=vittoria   (senza esito = tutte)
 * Risposta: [ { "id": 42, "esito": "vittoria", "avversario": "@lyra_eco", "round": 14 } ]   esito: vittoria | sconfitta | pareggio
 *
 * @param {*} esito
 * @param {AbortSignal} signal
 */
export function getCronologia(esito, signal) {
  return apiGet(`/partite${esito ? `?esito=${esito}` : ''}`, signal)
}

/**
 * Accessi consecutivi.
 * Risposta: { "giorno": 12, "totale": 30,
 *             "ricompense": [ { "giorno": 5, "nome": "Avatar", "sbloccata": true }, { "giorno": 15, "nome": "Cover per carte", "sbloccata": false } ] }
 *
 * @param {AbortSignal} signal
 */
export function getAccessi(signal) {
  return apiGet('/accessi', signal)
}
