// 🧪 MODALITÀ DEMO — backend finto, solo per provare l'app SENZA il backend vero.
// Si accende con VITE_DEMO=true (comando: npm run demo). Con il backend vero NON viene usato.
//
// Copre il flusso "partita casuale":
//   Home → Gioca → Scegli il mazzo → Matchmaking → Battaglia → Hai vinto/perso → Dettagli
// Le altre pagine (Collezione, Scambi...) in demo mostrano il messaggio di errore: è normale.
//
// I dati vivono in memoria: ricaricando la pagina (F5) si riparte da zero.

const ATTESA_MATCHMAKING_MS = 3000 // quanto dura "Stiamo simulando la partita"
const PV_INIZIALI = 10
const ROUND_MAX = 100
const CREDITI = { vittoria: 10, pareggio: 5, sconfitta: 2 }

// Le 8 carte di esempio del file card.html del team (cornice + illustrazione).
// Solo il Cadetto ha già l'illustrazione: le altre mostrano il segnaposto "ILLUSTRAZIONE".
// [codice, nome, atk, def, costo, eco, scuola, rarità, effetto, descrizione]
export const CARTE_ESEMPIO = [
  ['DOM01', 'Cadetto', 5, 4, 3, 'DOMINION', 'WARRIOR', 'COMMON', null, 'Giovane recluta del Dominio, ancora inesperta ma pronta a difendere la Corona con il suo giuramento appena pronunciato.'],
  ['DOM06', 'Martello del Re', 7, 4, 5, 'DOMINION', 'WARRIOR', 'LEGENDARY', 'PIERCING', null],
  ['SAP02', 'Custode dei Tomi', 3, 8, 4, 'WISDOM', 'MAGE', 'RARE', 'ANALYSIS', null],
  ['SAP07', 'Oracolo del Vuoto', 6, 5, 6, 'WISDOM', 'MAGE', 'LEGENDARY', 'CURSE', null],
  ['OBL02', 'Ombra dei Sepolcri', 4, 4, 3, 'OBLIVION', 'ROGUE', 'COMMON', null, 'Un’ombra silenziosa che vaga tra le tombe dimenticate, custode di ciò che nessuno ricorda più.'],
  ['OBL11', 'Boia del Crepuscolo', 6, 4, 4, 'OBLIVION', 'WARRIOR', 'LEGENDARY', 'BERSERKER', null],
  ['ING04', 'Lama Velata', 5, 5, 4, 'DECEPTION', 'ROGUE', 'RARE', 'DRAIN', null],
  ['ING01', 'Borseggiatore del Velo', 3, 4, 2, 'DECEPTION', 'ROGUE', 'COMMON', null, 'Agile e invisibile tra la folla, svuota le tasche prima che la vittima si accorga di qualcosa.'],
].map(([codice, nome, atk, def, costo, eco, scuola, rarita, effetto, descrizione]) => ({
  codice, nome, atk, def, costo, eco, scuola, rarita, effetto, descrizione,
  illustrazione: codice === 'DOM01' ? '/images/illustrazioni/DOM01.webp' : null,
}))

// ---------- Dati del giocatore ----------
const stato = {
  utente: { nome: 'Flavio', crediti: 1250, avatar: null, primoAccesso: false },
  partite: [], // le partite giocate in questa sessione
}

const mazzi = [
  { id: 1, nome: 'Mazzo del tramonto', valido: true, motivo: null, carte: 30, carteMax: 30, punti: 96, puntiMax: 98, scuole: ['Sapienza', 'Oblio'] },
  { id: 2, nome: 'Lama e Velo', valido: true, motivo: null, carte: 30, carteMax: 30, punti: 92, puntiMax: 98, scuole: ['Inganno'] },
  { id: 3, nome: 'Furia Rossa', valido: true, motivo: null, carte: 30, carteMax: 30, punti: 97, puntiMax: 98, scuole: ['Dominio'] },
  { id: 4, nome: 'Custodi', valido: false, motivo: '28/30 carte', carte: 28, carteMax: 30, punti: 80, puntiMax: 98, scuole: [] },
]

const avversari = ['@lyra_eco', '@ombra_sette', '@kael', '@nyx']

// ---------- La battaglia (versione semplificata delle regole) ----------
// Ogni round:
//   1. chi non ha una carta in campo pesca dal mazzo
//   2. le due carte si colpiscono NELLO STESSO MOMENTO: DEF = DEF − ATK avversario
//   3. una carta con DEF sotto 0 è distrutta e va negli scarti;
//      il danno in più (es. DEF −2 → 2) lo perde il giocatore in PV
//   4. si continua finché qualcuno arriva a 0 PV, finiscono le carte o si arriva al round 100

// Un numero a caso tra min e max (compresi)
const caso = (min, max) => min + Math.floor(Math.random() * (max - min + 1))

// Un mazzo di 30 carte pescate a caso tra quelle di esempio
// ({ ...carta } = una COPIA: così la DEF che scende in battaglia non cambia l'originale)
function creaMazzo() {
  return Array.from({ length: 30 }, () => {
    const carta = CARTE_ESEMPIO[caso(0, CARTE_ESEMPIO.length - 1)]
    return { ...carta, defStampata: carta.def } // defStampata = la DEF scritta sulla carta
  })
}

// "Fotografia" di un lato del campo, nel formato che si aspetta la pagina Battaglia
function foto(lato) {
  return {
    pv: Math.max(lato.pv, 0),
    carteMazzo: lato.mazzo.length,
    carteScarti: lato.scarti.length,
    // Tutti i dati della carta: la pagina la disegna a strati (cornice + illustrazione + testi)
    ultimaScartata: lato.scarti.at(-1) ? { ...lato.scarti.at(-1), def: lato.scarti.at(-1).defStampata } : null,
    carta: lato.campo ? { ...lato.campo } : null, // con la DEF attuale
  }
}

function simulaBattaglia(nomeAvversario) {
  const tu = { pv: PV_INIZIALI, mazzo: creaMazzo(), scarti: [], campo: null }
  const avv = { pv: PV_INIZIALI, mazzo: creaMazzo(), scarti: [], campo: null }
  const round = []

  for (let numero = 1; numero <= ROUND_MAX; numero++) {
    // 1. pesca (se serve)
    if (!tu.campo) tu.campo = tu.mazzo.pop() ?? null
    if (!avv.campo) avv.campo = avv.mazzo.pop() ?? null
    if (!tu.campo || !avv.campo) break // carte finite

    const inizio = { tu: foto(tu), avversario: foto(avv) }

    // 2. scontro: si colpiscono insieme
    const dannoTu = avv.campo.atk // danno che riceve la TUA carta
    const dannoAvversario = tu.campo.atk
    tu.campo.def -= dannoTu
    avv.campo.def -= dannoAvversario

    // 3. carte distrutte e danno in eccesso
    const eccessoTu = distruggiSeServe(tu)
    const eccessoAvv = distruggiSeServe(avv)

    round.push({
      numero,
      ...inizio,
      dannoTu,
      dannoAvversario,
      esito: testoEsito(eccessoTu, eccessoAvv, nomeAvversario),
      dopo: { tu: foto(tu), avversario: foto(avv) },
    })

    // 4. qualcuno è a 0 PV?
    if (tu.pv <= 0 || avv.pv <= 0) break
  }

  return { round, tu, avv }
}

// Se la carta in campo ha DEF sotto 0: va negli scarti e il giocatore perde PV.
// Restituisce null se la carta è ancora viva, altrimenti i PV persi.
function distruggiSeServe(lato) {
  if (lato.campo.def >= 0) return null
  const eccesso = -lato.campo.def
  lato.pv -= eccesso
  lato.scarti.push(lato.campo)
  lato.campo = null
  return eccesso
}

function testoEsito(eccessoTu, eccessoAvv, nomeAvversario) {
  const persi = (n) => (n > 0 ? `perdi ${n} PV` : 'nessun PV perso')
  if (eccessoTu !== null && eccessoAvv !== null) {
    return { titolo: 'Entrambe le carte sono distrutte', dettaglio: `Tu: ${persi(eccessoTu)} · ${nomeAvversario}: −${eccessoAvv} PV` }
  }
  if (eccessoTu !== null) return { titolo: 'La tua carta è distrutta', dettaglio: `Danno in eccesso: ${persi(eccessoTu)}` }
  if (eccessoAvv !== null) {
    return { titolo: 'Carta avversaria distrutta', dettaglio: `Danno in eccesso: ${nomeAvversario} perde ${eccessoAvv} PV` }
  }
  return null // nessuna carta distrutta: niente messaggio
}

// ---------- Creare una partita ----------
function avviaPartita(mazzoId) {
  const mazzo = mazzi.find((m) => m.id === mazzoId) ?? mazzi[0]
  const nomeAvversario = avversari[caso(0, avversari.length - 1)]
  const { round, tu, avv } = simulaBattaglia(nomeAvversario)

  const esito = tu.pv > avv.pv ? 'vittoria' : tu.pv < avv.pv ? 'sconfitta' : 'pareggio'
  const ultimo = round.at(-1)
  const partita = {
    id: stato.partite.length + 1,
    iniziataAlle: Date.now(),
    creditiDati: false,
    mioMazzo: mazzo.nome,
    avversario: { nome: nomeAvversario, mazzo: mazzi[caso(0, 2)].nome },
    esito,
    pvMiei: Math.max(tu.pv, 0),
    pvAvversario: Math.max(avv.pv, 0),
    creditiGuadagnati: CREDITI[esito],
    round: ultimo.numero,
    roundMax: ROUND_MAX,
    giocataIl: new Date().toISOString(),
    carteRimasteMie: tu.mazzo.length,
    carteRimasteAvversario: avv.mazzo.length,
    motivoFine: tu.pv <= 0 || avv.pv <= 0 ? 'PV a 0' : ultimo.numero === ROUND_MAX ? 'Round 100' : 'Carte finite',
    replay: { roundMax: ROUND_MAX, avversario: { nome: nomeAvversario }, round },
  }
  stato.partite.unshift(partita)
  return partita
}

// Come risponde GET /partite/:id: "in_corso" per i primi secondi, poi "conclusa"
function leggiPartita(partita) {
  const base = { id: partita.id, mioMazzo: partita.mioMazzo, avversario: partita.avversario }
  if (Date.now() - partita.iniziataAlle < ATTESA_MATCHMAKING_MS) return { ...base, stato: 'in_corso' }

  // La prima volta che risulta conclusa: aggiungi i crediti all'utente
  if (!partita.creditiDati) {
    stato.utente.crediti += partita.creditiGuadagnati
    partita.creditiDati = true
  }
  // Mandiamo tutto tranne i campi "interni" della demo (replay, orario, crediti dati)
  const dati = { ...partita }
  delete dati.replay
  delete dati.iniziataAlle
  delete dati.creditiDati
  return { ...dati, stato: 'conclusa' }
}

// ---------- Il "centralino": percorso → risposta ----------
// Riceve le stesse cose di una fetch vera e restituisce i dati (o lancia un errore 404).
export async function rispostaDemo(metodo, percorso, corpo) {
  await new Promise((fatto) => setTimeout(fatto, 250)) // piccolo ritardo, come una rete vera
  const [indirizzo, query] = percorso.split('?')
  const parametri = new URLSearchParams(query)
  const pezzi = indirizzo.split('/').filter(Boolean) // "/partite/3/replay" → ["partite", "3", "replay"]
  const richiesta = `${metodo} /${pezzi[0] ?? ''}`

  const concluse = () => stato.partite.filter((p) => Date.now() - p.iniziataAlle >= ATTESA_MATCHMAKING_MS)

  if (richiesta === 'GET /utente') return { ...stato.utente }

  if (richiesta === 'GET /dashboard') {
    return {
      richiesteNuove: 0,
      leggendaria: { buste: 7, totale: 10 },
      ultimePartite: concluse()
        .slice(0, 3)
        .map((p) => ({ id: p.id, esito: p.esito, avversario: p.avversario.nome, round: p.round })),
      prove: { completate: 3, totali: 12, inEvidenza: [{ id: 1, titolo: 'Vinci 10 partite', ricompensa: '20 crediti', fatto: 4, totale: 10 }] },
    }
  }

  if (richiesta === 'GET /mazzi') return { totale: mazzi.length, massimo: 20, mazzi }

  if (richiesta === 'POST /partite') return { id: avviaPartita(corpo?.mazzoId).id }

  if (richiesta === 'GET /partite' && pezzi.length === 1) {
    // Cronologia: /partite?esito=vittoria
    const esito = parametri.get('esito')
    return concluse()
      .filter((p) => !esito || p.esito === esito)
      .map((p) => ({ id: p.id, esito: p.esito, avversario: p.avversario.nome, round: p.round }))
  }

  if (pezzi[0] === 'partite') {
    const partita = stato.partite.find((p) => p.id === Number(pezzi[1]))
    if (partita && metodo === 'GET' && pezzi[2] === 'replay') return partita.replay
    if (partita && metodo === 'GET' && !pezzi[2]) return leggiPartita(partita)
  }

  // Tutto il resto non esiste in demo → stesso errore di un 404 vero
  const errore = new Error(`[demo] ${metodo} ${percorso} non esiste in modalità demo`)
  errore.tipo = 'richiesta'
  errore.stato = 404
  throw errore
}
