import { apiGet, apiPost } from './client'
import { RARITA_PER_LIVELLO, cartaDalBackend } from './backend'

/**
 * Buste da aprire e progresso verso la leggendaria garantita.
 * ✅ COLLEGATO:
 *   GET /private/packs/purchases/unopened                       → [{ purchaseId, ... }]
 *   GET /private/packs/purchases/{id}/legendary-guarantee-progress
 *       → { cycleLength, completedOpeningsInCurrentCycle, openingsUntilGuaranteedLegendary, ... }
 * @param {AbortSignal} signal
 * @returns {Promise<{ disponibili: number, acquisti: object[], leggendaria: { buste: number, totale: number } }>}
 */
export async function getBuste(signal) {
  const acquisti = (await apiGet('/private/packs/purchases/unopened', signal)) ?? []
  let leggendaria = { buste: 0, totale: 10 }
  if (acquisti.length > 0) {
    try {
      const p = await apiGet(`/private/packs/purchases/${acquisti[0].purchaseId}/legendary-guarantee-progress`, signal)
      leggendaria = { buste: p.completedOpeningsInCurrentCycle, totale: p.cycleLength }
    } catch (errore) {
      if (errore.name === 'AbortError') throw errore
      // se il progresso non arriva, mostriamo comunque le buste
    }
  }
  return { disponibili: acquisti.length, acquisti, leggendaria }
}

/**
 * Apre una o più buste (una alla volta, dalla più vecchia).
 * ✅ COLLEGATO: POST /private/packs/purchases/{purchaseId}/open → { cards: [CardResponse], ... }
 * @param {number} quantita - quante buste aprire
 * @returns {Promise<{ carte: object[], riepilogo: string }>}
 */
export async function apriBuste(quantita) {
  const { acquisti } = await getBuste()
  const daAprire = acquisti.slice(0, quantita)
  const carte = []
  for (const acquisto of daAprire) {
    const apertura = await apiPost(`/private/packs/purchases/${acquisto.purchaseId}/open`)
    carte.push(...(apertura?.cards ?? []).map(cartaDalBackend))
  }
  return { carte, riepilogo: riepilogo(carte) }
}

/** Frase tipo "3 comuni, 1 rara e 1 leggendaria." */
function riepilogo(carte) {
  const nomi = { Comune: ['comune', 'comuni'], Rara: ['rara', 'rare'], Leggendaria: ['leggendaria', 'leggendarie'] }
  const parti = Object.values(RARITA_PER_LIVELLO)
    .map((r) => [r, carte.filter((c) => c.rarita === r).length])
    .filter(([, n]) => n > 0)
    .map(([r, n]) => `${n} ${nomi[r][n === 1 ? 0 : 1]}`)
  if (parti.length === 0) return ''
  return parti.length === 1 ? `${parti[0]}.` : `${parti.slice(0, -1).join(', ')} e ${parti.at(-1)}.`
}
