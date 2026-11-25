import { apiPost } from './client'
import { NOMI_ECHI, NOMI_RARITA, NOMI_SCUOLE, cartaDalBackend, tutteLePagine } from './backend'

/**
 * Tutte le carte possedute, già nel formato delle pagine.
 * ✅ COLLEGATO: GET /private/collections (a pagine da 10: le scarichiamo tutte)
 * @param {AbortSignal} signal
 * @returns {Promise<object[]>}
 */
export async function getTutteLeCarte(signal) {
  const { elementi } = await tutteLePagine('/private/collections', signal, 'sortBy=card.id&direction=ASC')
  return elementi.map(cartaDalBackend)
}

/**
 * Le carte della collezione con i filtri applicati.
 * ⚠️ Il backend non ha ancora i filtri: li applichiamo qui nel browser.
 * @param {{ scuola: string[], eco: string[], rarita: string[], abilita: string[] }} filtri
 * @param {AbortSignal} signal
 * @returns {Promise<{ totale: number, trovate: number, carte: object[] }>}
 */
export async function getCollezione(filtri, signal) {
  const tutte = await getTutteLeCarte(signal)
  // Un filtro vuoto = "tutti". Altrimenti il valore della carta deve essere tra quelli scelti.
  const passa = (valori, valore) => valori.length === 0 || valori.includes(valore)
  const carte = tutte.filter(
    (carta) =>
      passa(filtri.scuola, carta.scuola) &&
      passa(filtri.eco, carta.eco) &&
      passa(filtri.rarita, carta.rarita) &&
      passa(filtri.abilita, carta.abilita),
  )
  return { totale: tutte.length, trovate: carte.length, carte }
}

/**
 * I valori possibili per ogni filtro (sono fissi, come nel database del backend).
 * ⚠️ "abilita" è vuoto: il backend non manda ancora l'effetto delle carte.
 * @returns {Promise<{ scuola: string[], eco: string[], rarita: string[], abilita: string[] }>}
 */
export async function getFiltriCollezione() {
  return { scuola: NOMI_SCUOLE, eco: NOMI_ECHI, rarita: NOMI_RARITA, abilita: [] }
}

/**
 * Tutti i dati di UNA carta della collezione.
 * ⚠️ Il backend non ha ancora "GET carta per id": la cerchiamo nella collezione.
 * @param {string|number} id
 * @param {AbortSignal} signal
 */
export async function getCarta(id, signal) {
  const carta = (await getTutteLeCarte(signal)).find((c) => String(c.id) === String(id))
  if (!carta) {
    const errore = new Error(`Carta ${id} non trovata nella collezione`)
    errore.tipo = 'richiesta'
    errore.stato = 404
    throw errore
  }
  return {
    ...carta,
    // ⚠️ DA FARE NEL BACKEND: copie nei mazzi e conversione dei doppioni
    copieNeiMazzi: 0,
    conversione: null,
  }
}

/**
 * ⚠️ NON ANCORA NEL BACKEND: converte i doppioni in crediti.
 * Risposta attesa: { "creditiAggiunti": 10, "quantita": 2 }
 */
export function convertiCopie(id) {
  return apiPost(`/collezione/carte/${id}/converti`)
}
