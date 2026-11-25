import { apiGet } from './client'
import { getBuste } from './buste'
import { getRichieste } from './richieste'

/**
 * I dati della Home. ⚠️ GET /dashboard NON è ancora nel backend:
 * se non risponde, i dati vengono presi dagli altri endpoint (vedi sotto).
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
export async function getDashboard(signal) {
  try {
    return await apiGet('/dashboard', signal)
  } catch (errore) {
    if (errore.name === 'AbortError') throw errore
    return dashboardDaAltriEndpoint(signal)
  }
}

/**
 * ⚠️ Il backend non ha ancora /dashboard: costruiamo la Home con gli endpoint che esistono.
 *   ✅ richieste nuove       ← GET /private/friendships/requests/received
 *   ✅ leggendaria garantita ← GET /private/packs/purchases/unopened (+ progresso)
 *   ⏳ ultime partite e Prove degli Echi: null → la Home mostra "Presto disponibile"
 * Se anche questi endpoint non rispondono, la Home mostra comunque saluto e GIOCA.
 * @param {AbortSignal} signal
 * @returns {Promise<{ richiesteNuove: number, leggendaria: object|null, ultimePartite: null, prove: null }>}
 */
async function dashboardDaAltriEndpoint(signal) {
  const [richieste, buste] = await Promise.allSettled([getRichieste(signal), getBuste(signal)])
  return {
    richiesteNuove: richieste.status === 'fulfilled' ? richieste.value.length : 0,
    leggendaria: buste.status === 'fulfilled' ? buste.value.leggendaria : null,
    ultimePartite: null,
    prove: null,
  }
}
