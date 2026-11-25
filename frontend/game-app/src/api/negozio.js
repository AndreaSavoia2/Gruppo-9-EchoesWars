import { apiGet, apiPost } from './client'
import { getBuste } from './buste'

// ⚠️ DA FARE NEL BACKEND: il prezzo non arriva nella lista dei pacchetti.
// Nel database il "Pacchetto Base" costa 300 crediti: lo usiamo finché il backend non lo manda.
const PREZZO_PROVVISORIO = 300

/**
 * I prodotti del negozio, le buste da aprire e il progresso verso la leggendaria.
 * ✅ COLLEGATO:
 *   GET /private/packs                     → [{ id, code, nameIt, descriptionIt, ... }]
 *   GET /private/packs/purchases/unopened  → buste comprate e non ancora aperte
 * @param {AbortSignal} signal
 */
export async function getNegozio(signal) {
  const [pacchetti, buste] = await Promise.all([apiGet('/private/packs', signal), getBuste(signal)])
  return {
    prodotti: (pacchetti ?? []).map((p) => ({
      id: p.id,
      nome: p.nameIt,
      immagine: '/images/busta.png',
      descrizione: p.descriptionIt,
      dettaglio: null,
      prezzo: p.priceCredits ?? PREZZO_PROVVISORIO,
    })),
    // ⚠️ Testo fisso dalle regole del gioco (il backend non lo manda)
    regoleCrediti: 'Vittoria +10 · Pareggio +5 · Sconfitta +2',
    busteDaAprire: buste.disponibili,
    leggendaria: buste.leggendaria,
  }
}

/**
 * Compra un pacchetto.
 * ✅ COLLEGATO: POST /private/packs/{id}/purchases → { purchaseId, remainingBalance, ... }
 * (i crediti nuovi li rilegge la barra in alto)
 * @param {number} prodottoId - id del pacchetto
 */
export function compra(prodottoId) {
  return apiPost(`/private/packs/${prodottoId}/purchases`)
}
