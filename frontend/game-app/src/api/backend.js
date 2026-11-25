import { apiGet } from './client'

// 🔁 "Traduttori" dal formato del backend Spring al formato che usano le pagine.
// Così, se il backend cambia un nome di campo, si corregge SOLO qui.

/** Rarità per livello (tabella "tiers" del backend: 1 Comune, 2 Rara, 3 Leggendaria). */
export const RARITA_PER_LIVELLO = { 1: 'Comune', 2: 'Rara', 3: 'Leggendaria' }

/** Id delle Eco nel backend (tabella "factions"). Servono per creare un mazzo. */
export const ID_ECHI = { Dominio: 1, Sapienza: 2, Oblio: 3, Inganno: 4 }

/** Nomi italiani usati dal backend (servono per i filtri). */
export const NOMI_ECHI = ['Dominio', 'Sapienza', 'Oblio', 'Inganno']
export const NOMI_SCUOLE = ['Guerriero', 'Mago', 'Ladro']
export const NOMI_RARITA = ['Comune', 'Rara', 'Leggendaria']

/**
 * Trasforma una carta del backend (CardResponse) nel formato delle pagine.
 * @param {object} c - carta come arriva dal backend
 * @returns {object} carta pronta per <CartaGioco> e per le pagine
 */
export function cartaDalBackend(c) {
  return {
    id: c.id,
    codice: c.code,
    nome: c.nameIt,
    descrizione: c.descriptionIt,
    atk: c.attack,
    def: c.defense,
    costo: c.cost,
    punti: c.cost, // nel mazzo i "punti" sono la somma dei costi (max 98)
    eco: c.echoesNameIt,
    scuola: c.schoolNameIt,
    rarita: RARITA_PER_LIVELLO[c.tierLevel] ?? 'Comune',
    quantita: c.quantity ?? 0,
    illustrazione: c.artUrl || null,
    cornice: c.frameImageUrl || null, // se manca, <CartaGioco> usa le cornici locali
    // ⚠️ Il backend non manda ancora l'effetto della carta (es. "PIERCING")
    effetto: null,
    abilita: null,
  }
}

/**
 * Scarica TUTTE le pagine di una lista paginata del backend e le unisce.
 * Il backend risponde { content, totalPages, totalElements, currentPage }
 * e dà al massimo 10 elementi per pagina.
 * @param {string} percorso - es. "/private/collections"
 * @param {AbortSignal} signal - per annullare se la pagina viene chiusa
 * @param {string} [ordine] - parametri di ordinamento, es. "sortBy=card.id&direction=ASC"
 * @returns {Promise<{ elementi: object[], totale: number }>}
 */
export async function tutteLePagine(percorso, signal, ordine = '') {
  const elementi = []
  let pagina = 0
  let totalePagine = 1
  let totale = 0
  // Massimo 100 pagine (= 1000 elementi): un freno di sicurezza
  while (pagina < totalePagine && pagina < 100) {
    const dati = await apiGet(`${percorso}?pageNumber=${pagina}&pageSize=10${ordine ? `&${ordine}` : ''}`, signal)
    elementi.push(...(dati?.content ?? []))
    totalePagine = dati?.totalPages ?? 0
    totale = dati?.totalElements ?? elementi.length
    pagina++
  }
  return { elementi, totale }
}
