import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// Risposta attesa:
// {
//   "prodotti": [ { "id": 1, "nome": "Busta standard", "immagine": "https://.../busta.png",
//                   "descrizione": "5 carte: 3 comuni, 1 rara, 1 casuale",
//                   "dettaglio": "Comune 85% · Rara 13% · Leggendaria 2%",   // probabilità (facoltativo)
//                   "prezzo": 100 } ],
//   "regoleCrediti": "Vittoria +10 · Pareggio +5 · Sconfitta +2 (max 40 al giorno)",
//   "busteDaAprire": 3,
//   "leggendaria": { "buste": 7, "totale": 10 }
// }
export function getNegozio(signal) {
  return apiGet('/negozio', signal)
}

// Compra un prodotto. Invia: { "prodottoId": 1 }
// Risposta: { "busteDaAprire": 4 }    (i crediti nuovi li rilegge la barra in alto)
export function compra(prodottoId) {
  return apiPost('/negozio/compra', { prodottoId })
}
