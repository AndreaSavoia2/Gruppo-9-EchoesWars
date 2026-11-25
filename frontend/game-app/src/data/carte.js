// 🃏 Tutto quello che serve per DISEGNARE una carta (cornice + illustrazione + testi).
// Preso dal file "card.html" del team. Le misure sono in "punti carta":
// la carta è grande 750 × 1050 punti, qualunque sia la sua dimensione sullo schermo.

// Il backend può mandare i nomi in inglese (come nel file del team) o in italiano:
// questi dizionari li traducono sempre in italiano.
export const ECHI = { DOMINION: 'Dominio', WISDOM: 'Sapienza', OBLIVION: 'Oblio', DECEPTION: 'Inganno' }
export const SCUOLE = { WARRIOR: 'Guerrieri', MAGE: 'Maghi', ROGUE: 'Furfanti' }
export const RARITA = { COMMON: 'Comune', RARE: 'Rara', LEGENDARY: 'Leggendaria' }

// Gli effetti: nome + spiegazione (testi delle regole)
export const EFFETTI = {
  PIERCING: ['Perforante', 'I suoi attacchi ignorano le riduzioni dirette ai danni, come Resistenza, ma non ignorano DEF o Protezione.'],
  ANALYSIS: ['Analisi', 'Contro un avversario con ATK base stampato maggiore del proprio, ottiene 1 punto di Protezione una sola volta per quell’avversario. La Protezione dura fino al consumo o alla fine dell’incontro.'],
  CURSE: ['Maledizione', 'Quando entra in campo, riduce di 2 la DEF residua della carta avversaria già in campo, fino a un minimo di 1. Non distrugge la carta né danneggia i PV; ignora la Protezione senza consumarla.'],
  BERSERKER: ['Berserker', 'Dal secondo round in campo, all’inizio del round perde 1 DEF residua per ottenere +2 ATK, fino a un massimo di +4 ATK. Non riduce la DEF sotto 1; se non può pagare la DEF, non ottiene il bonus.'],
  DRAIN: ['Drenaggio', 'Quando sconfigge una carta avversaria e sopravvive alla risoluzione completa, l’avversario scarta la carta in cima al proprio mazzo. Non si attiva se la carta è distrutta da Riflesso.'],
}

// Le "zone" di ogni cornice (le immagini arrivano dal backend; in public/images/cornici
// ci sono solo copie per la modalità demo).
// ⚠️ Quando arriva una cornice NUOVA, va aggiunta qui con le sue misure (le trovi
// nel file card.html del team, oggetto "Z"). Il nome deve essere uguale al nome del file.
//   nome:  [x0, y0, x1, y1]  targhetta del nome in alto
//   testo: [x0, y0, x1, y1]  pergamena con descrizione/effetto
//   fondoArte: y dove finisce l'illustrazione
//   arte:  [x0, yInizio, x1] finestra dell'illustrazione
// Ogni cornice è disegnata a mano, quindi le zone cambiano un po' da una all'altra.
export const CORNICI = {
  dominio_comune: { nome: [176, 43, 640, 103], testo: [54, 658, 698, 903], fondoArte: 567, arte: [44, 120, 706] },
  dominio_leggendaria: { nome: [184, 43, 653, 105], testo: [54, 726, 692, 926], fondoArte: 630, arte: [44, 115, 706] },
  sapienza_rara: { nome: [175, 38, 670, 106], testo: [46, 678, 705, 913], fondoArte: 588, arte: [44, 118, 706] },
  sapienza_leggendaria: { nome: [183, 43, 651, 105], testo: [59, 726, 685, 930], fondoArte: 630, arte: [44, 115, 706] },
  oblio_comune_rogue: { nome: [177, 39, 666, 106], testo: [46, 656, 704, 905], fondoArte: 567, arte: [44, 117, 706] },
  oblio_leggendaria_warrior: { nome: [173, 40, 660, 104], testo: [50, 724, 698, 938], fondoArte: 628, arte: [44, 114, 706] },
  inganno_rara: { nome: [175, 36, 666, 106], testo: [52, 723, 697, 934], fondoArte: 630, arte: [44, 104, 706] },
  inganno_comune: { nome: [186, 36, 621, 105], testo: [60, 728, 685, 933], fondoArte: 633, arte: [44, 116, 706] },
}

// Traduce un valore con il dizionario (se è già in italiano lo lascia com'è)
const traduci = (dizionario, valore) => dizionario[valore] ?? valore

// Prepara i dati della carta in un formato unico, tutto in italiano.
// Esempio: { eco: "DOMINION", scuola: "WARRIOR" } → { eco: "Dominio", scuola: "Guerrieri" }
export function normalizzaCarta(carta) {
  const effetto = carta.effetto ? EFFETTI[carta.effetto] : null
  return {
    ...carta,
    eco: traduci(ECHI, carta.eco),
    scuola: traduci(SCUOLE, carta.scuola),
    rarita: traduci(RARITA, carta.rarita),
    // effetto: il backend può mandare il codice ("PIERCING") o già { nome, testo }
    effetto: effetto ? { nome: effetto[0], testo: effetto[1] } : carta.effetto ?? null,
  }
}

// Quale cornice usare e dove prenderla.
// Restituisce { nome, src }:
//   nome → serve per trovare le "zone" in CORNICI (dove vanno nome, testo, illustrazione)
//   src  → l'indirizzo dell'immagine
//
// 1. Il backend manda "cornice": "https://.../cornici/dominio_comune.webp"
//    → usiamo quell'immagine, e il nome lo prendiamo dal file ("dominio_comune")
// 2. Il backend non la manda (es. modalità demo)
//    → la scegliamo noi tra quelle in public/images/cornici, in base a Eco + rarità + scuola
const SCUOLA_FILE = { Guerrieri: 'warrior', Maghi: 'mage', Furfanti: 'rogue' }

export function sceltaCornice({ cornice, eco, rarita, scuola }) {
  if (cornice) {
    // ".../cornici/dominio_comune.webp?v=2" → "dominio_comune"
    const nomeFile = cornice.split('/').pop().split(/[?#]/)[0].replace(/\.[a-z]+$/i, '')
    // Se è una cornice nuova che non conosciamo, usiamo le zone di una simile (stessa Eco)
    const nome = CORNICI[nomeFile] ? nomeFile : cerca(eco, rarita, scuola)
    return { nome, src: cornice }
  }
  const nome = cerca(eco, rarita, scuola)
  return { nome, src: `/images/cornici/${nome}.webp` }
}

// Cerca dalla più precisa alla più generica:
//   1. eco + rarità + scuola  (es. "oblio_comune_rogue")
//   2. eco + rarità           (es. "dominio_comune")
//   3. una qualsiasi della stessa eco (finché non ci sono tutte le cornici)
function cerca(eco = '', rarita = '', scuola) {
  const e = eco.toLowerCase()
  const r = rarita.toLowerCase()
  const candidati = [`${e}_${r}_${SCUOLA_FILE[scuola]}`, `${e}_${r}`]
  const trovata = candidati.find((nome) => CORNICI[nome]) ?? Object.keys(CORNICI).find((nome) => nome.startsWith(`${e}_`))
  return trovata ?? 'dominio_comune'
}
