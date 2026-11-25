import { apiGet, apiPost, apiPut } from './client'
import { ID_ECHI, NOMI_ECHI, NOMI_SCUOLE, cartaDalBackend } from './backend'
import { getTutteLeCarte } from './collezione'
import { REGOLE_MAZZO } from '../data/regole'

// ⚠️ DA CONFERMARE col backend: quanti mazzi si possono avere al massimo
const MASSIMO_MAZZI = 20

/**
 * Il dettaglio di un mazzo, così come arriva dal backend.
 * ✅ COLLEGATO: GET /private/decks/{id}
 *   → { id, name, status, totalCards, totalCost, factions: [{ id, nameIt }], cards: [CardResponse] }
 *   (in "cards", quantity = copie di quella carta NEL MAZZO)
 */
function getDettaglio(id, signal) {
  return apiGet(`/private/decks/${id}`, signal)
}

/**
 * Trasforma il dettaglio di un mazzo nel formato della lista mazzi.
 * Un mazzo è "valido" se ha esattamente 30 carte e al massimo 98 punti.
 */
function mazzoDalBackend(d) {
  const motivi = []
  if (d.totalCards !== REGOLE_MAZZO.carte) motivi.push(`${d.totalCards}/${REGOLE_MAZZO.carte} carte`)
  if (d.totalCost > REGOLE_MAZZO.punti) motivi.push(`${d.totalCost}/${REGOLE_MAZZO.punti} punti`)
  return {
    id: d.id,
    nome: d.name,
    valido: motivi.length === 0,
    motivo: motivi[0] ?? null,
    carte: d.totalCards,
    carteMax: REGOLE_MAZZO.carte,
    punti: d.totalCost,
    puntiMax: REGOLE_MAZZO.punti,
    scuole: (d.factions ?? []).map((f) => f.nameIt), // le 2 Eco del mazzo
    scuoleCarte: [...new Set((d.cards ?? []).map((c) => c.schoolNameIt))], // per il filtro "Scuola"
  }
}

/**
 * I mazzi del giocatore, con filtri e ordinamento (applicati qui nel browser).
 * ✅ COLLEGATO: GET /private/decks?status=READY + il dettaglio di ogni mazzo
 *   (la lista del backend non ha carte e punti, il dettaglio sì)
 * @param {AbortSignal} signal
 * @param {{ scuola?: string[], eco?: string[], ordina?: string[] }} [filtri]
 * @returns {Promise<{ totale: number, massimo: number, mazzi: object[] }>}
 */
export async function getMazzi(signal, filtri = {}) {
  const lista = (await apiGet('/private/decks?status=READY', signal)) ?? []
  const dettagli = await Promise.all(lista.map((m) => getDettaglio(m.id, signal)))
  const tutti = dettagli.map(mazzoDalBackend)

  const scuola = filtri.scuola ?? []
  const eco = filtri.eco ?? []
  let mazzi = tutti.filter(
    (m) =>
      (scuola.length === 0 || scuola.some((s) => m.scuoleCarte.includes(s))) &&
      (eco.length === 0 || eco.some((e) => m.scuole.includes(e))),
  )

  const ordina = [].concat(filtri.ordina ?? [])[0]
  if (ordina === 'Nome (A-Z)') mazzi = [...mazzi].sort((a, b) => a.nome.localeCompare(b.nome, 'it'))
  else if (ordina === 'Punti') mazzi = [...mazzi].sort((a, b) => b.punti - a.punti)
  else mazzi = [...mazzi].sort((a, b) => b.id - a.id) // "Più recenti" (id più alto = più nuovo)

  return { totale: tutti.length, massimo: MASSIMO_MAZZI, mazzi }
}

/**
 * I valori possibili dei filtri della lista mazzi (fissi).
 * @returns {Promise<{ scuola: string[], eco: string[], ordina: string[] }>}
 */
export async function getFiltriMazzi() {
  return { scuola: NOMI_SCUOLE, eco: NOMI_ECHI, ordina: ['Più recenti', 'Nome (A-Z)', 'Punti'] }
}

/**
 * Un mazzo da modificare.
 * ✅ COLLEGATO: GET /private/decks/{id}
 * @returns {Promise<{ id: number, nome: string, echi: string[], carte: { cartaId: number, copie: number }[] }>}
 */
export async function getMazzo(id, signal) {
  const d = await getDettaglio(id, signal)
  return {
    id: d.id,
    nome: d.name,
    echi: (d.factions ?? []).map((f) => f.nameIt),
    carte: (d.cards ?? []).map((c) => ({ cartaId: c.id, copie: c.quantity ?? 1, carta: cartaDalBackend(c) })),
  }
}

/**
 * Le carte che POSSIEDI delle 2 Eco scelte (per costruire il mazzo).
 * ✅ COLLEGATO: usa la collezione (GET /private/collections) e filtra per Eco.
 * @param {string[]} echi - es. ["Sapienza", "Oblio"]
 */
export async function getCarteDisponibili(echi, signal) {
  const tutte = await getTutteLeCarte(signal)
  return tutte.filter((carta) => echi.includes(carta.eco))
}

/**
 * Salva il mazzo: nuovo (POST) oppure modificato (PUT).
 * ✅ COLLEGATO:
 *   POST /private/decks      { name, factionIds: [1, 3], cards: [{ cardId, quantity }] }
 *   PUT  /private/decks/{id} { name, cards: [{ cardId, quantity }] }
 * ⚠️ Il CORS del backend oggi permette solo GET e POST: la MODIFICA (PUT) sarà bloccata
 *    finché il backend non aggiunge PUT e DELETE.
 * @param {{ nome: string, echi: string[], carte: { cartaId: number, copie: number }[] }} mazzo
 * @param {string|number} [id] - se c'è, modifica quel mazzo
 * @returns {Promise<{ id: number }>}
 */
export async function salvaMazzo(mazzo, id) {
  const cards = mazzo.carte.map((c) => ({ cardId: c.cartaId, quantity: c.copie }))
  const salvato = id
    ? await apiPut(`/private/decks/${id}`, { name: mazzo.nome, cards })
    : await apiPost('/private/decks', { name: mazzo.nome, factionIds: mazzo.echi.map((e) => ID_ECHI[e]), cards })
  return { id: salvato?.id }
}
