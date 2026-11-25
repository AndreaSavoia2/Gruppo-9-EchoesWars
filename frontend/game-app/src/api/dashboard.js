import { apiGet } from './client'

/**
 * ⚠️ DA CONFERMARE col backend: percorso e forma dei dati.
 *
 * Risposta attesa:
 * {
 *   "richiesteNuove": 2,                       // numero di richieste non lette
 *   "leggendaria": { "buste": 7, "totale": 10 },
 *   "ultimePartite": [
 *     { "id": 1, "esito": "vittoria", "avversario": "@nome", "round": 14 }
 *   ],                                         // esito: "vittoria" oppure "sconfitta"
 *   "prove": {
 *     "completate": 3,
 *     "totali": 12,
 *     "inEvidenza": [
 *       { "id": 1, "titolo": "...", "ricompensa": "10 crediti", "fatto": 0, "totale": 1 }
 *     ]
 *   }
 * }
 *
 * @param {AbortSignal} signal
 */
export function getDashboard(signal) {
  return apiGet('/dashboard', signal)
}
