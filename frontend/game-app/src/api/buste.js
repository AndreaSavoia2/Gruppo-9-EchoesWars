import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// Buste da aprire e progresso verso la leggendaria garantita.
// Risposta attesa: { "disponibili": 3, "leggendaria": { "buste": 7, "totale": 10 } }
export function getBuste(signal) {
  return apiGet('/buste', signal)
}

// Apre una o più buste.
// Invia: { "quantita": 1 }
// Risposta attesa:
// {
//   "carte": [ { "id": 1, "nome": "Nome carta", "immagine": "https://...", "rarita": "Comune" } ],
//   "riepilogo": "3 comuni, 1 rara e la casuale è rara."
// }
export function apriBuste(quantita) {
  return apiPost('/buste/apri', { quantita })
}
