import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// Le carte della collezione, già filtrate dal backend.
// I filtri vanno nell'indirizzo, uno per valore scelto:
//   /collezione?scuola=Maghi&scuola=Furfanti&rarita=Rara
// Risposta attesa:
// {
//   "totale": 124,     // tutte le carte della collezione
//   "trovate": 3,      // quelle che rispettano i filtri
//   "carte": [ { "id": 1, "nome": "Nome carta", "immagine": "https://...", "quantita": 2 } ]
// }
export function getCollezione(filtri, signal) {
  const parametri = new URLSearchParams()
  for (const [nome, valori] of Object.entries(filtri)) {
    valori.forEach((valore) => parametri.append(nome, valore))
  }
  const query = parametri.toString()
  return apiGet(`/collezione${query ? `?${query}` : ''}`, signal)
}

// I valori possibili per ogni filtro.
// Risposta attesa:
// { "scuola": ["Guerrieri", "Maghi", "Furfanti"], "eco": [...], "rarita": [...], "abilita": [...] }
export function getFiltriCollezione(signal) {
  return apiGet('/collezione/filtri', signal)
}

// Tutti i dati di UNA carta della collezione.
// Risposta attesa:
// {
//   "id": 1, "nome": "Borseggiatore del velo", "immagine": "https://...",
//   "quantita": 12,                    // copie possedute
//   "copieNeiMazzi": 2,                // copie usate nei mazzi attivi
//   "eco": "Inganno",                  // Dominio | Sapienza | Inganno | Oblio
//   "scuola": "Furfanti",
//   "rarita": "Rara",                  // Comune | Rara | Leggendaria
//   "atk": 3, "def": 4, "costo": 2,
//   "abilita": "Drenaggio",
//   "conversione": { "copie": 10, "crediti": 10 }   // null se non puoi convertire
// }
export function getCarta(id, signal) {
  return apiGet(`/collezione/carte/${id}`, signal)
}

// Converte i doppioni in crediti.
// Risposta attesa: { "creditiAggiunti": 10, "quantita": 2 }   ← quantita = copie rimaste
export function convertiCopie(id) {
  return apiPost(`/collezione/carte/${id}/converti`)
}
