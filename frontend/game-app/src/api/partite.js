import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// Avvia una partita casuale con il mazzo scelto.
// Invia:    { "modalita": "casuale", "mazzoId": 1 }
// Risposta: { "id": 42 }   ← l'id della partita appena creata
export function avviaPartitaCasuale(mazzoId) {
  return apiPost('/partite', { modalita: 'casuale', mazzoId })
}

// Stato e risultato di una partita.
// Risposta attesa:
// {
//   "id": 42,
//   "stato": "in_corso",            // "in_corso", "conclusa" oppure "interrotta"
//                                   // (interrotta = la connessione è caduta: il sistema la finisce da solo)
//   "mioMazzo": "Nome del mio mazzo",
//   "avversario": { "nome": "@nome" },
//   // questi arrivano solo quando stato = "conclusa":
//   "esito": "vittoria",             // "vittoria" oppure "sconfitta"
//   "pvMiei": 6, "pvAvversario": 0,  // punti vita finali
//   "creditiGuadagnati": 10,
//   "round": 14,
//   // per la pagina Dettagli:
//   "giocataIl": "2026-11-22T21:30:00Z",   // data e ora della partita
//   "roundMax": 100,
//   "avversario": { "nome": "@nome", "mazzo": "Nome mazzo avversario" },
//   "carteRimasteMie": 19, "carteRimasteAvversario": 17,
//   "motivoFine": "PV a 0"                  // come è finita
// }
export function getPartita(id, signal) {
  return apiGet(`/partite/${id}`, signal)
}

// Battaglia animata della partita, round per round.
// Serve sia subito dopo il matchmaking (/partita/:id/battaglia) sia per "Rivedi partita".
// Ogni round ha 3 momenti: carte in campo → scontro → esito.
// Risposta attesa:
// {
//   "roundMax": 100,
//   "avversario": { "nome": "@nome" },
//   "round": [
//     {
//       "numero": 3,
//       // situazione all'INIZIO del round (carte in campo)
//       "tu":         { "pv": 10, "carteMazzo": 25, "carteScarti": 2,
//                       "ultimaScartata": "https://.../carta.png",    // immagine, o null
//                       "carta": { "nome": "Nome carta", "immagine": "https://...", "def": 2 } },
//       "avversario": { ...come "tu"... },
//       // danni subiti durante lo scontro (0 = nessuno)
//       "dannoTu": 3, "dannoAvversario": 5,
//       // messaggio di fine round (o null)
//       "esito": { "titolo": "La tua carta è distrutta", "dettaglio": "Danno in eccesso: perdi 1 PV" },
//       // situazione alla FINE del round ("carta": null se è stata distrutta)
//       "dopo": { "tu": { ... }, "avversario": { ... } }
//     }
//   ]
// }
export function getReplay(id, signal) {
  return apiGet(`/partite/${id}/replay`, signal)
}
