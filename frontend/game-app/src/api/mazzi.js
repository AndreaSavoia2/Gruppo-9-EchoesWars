import { apiGet, apiPost, apiPut } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// I mazzi del giocatore. Filtri e ordinamento (facoltativi) vanno nell'indirizzo:
//   /mazzi?scuola=Maghi&eco=Inganno&ordina=recenti
// Risposta attesa:
// {
//   "totale": 12,        // quanti mazzi hai
//   "massimo": 20,       // quanti ne puoi avere al massimo
//   "mazzi": [
//     {
//       "id": 1,
//       "nome": "Nome del mazzo",
//       "valido": true,
//       "motivo": null,               // se NON valido: perché (es. "28/30 carte")
//       "carte": 30, "carteMax": 30,
//       "punti": 96, "puntiMax": 98,
//       "scuole": ["Sapienza", "Oblio"]
//     }
//   ]
// }
export function getMazzi(signal, filtri = {}) {
  const parametri = new URLSearchParams()
  for (const [nome, valori] of Object.entries(filtri)) {
    ;[].concat(valori).forEach((valore) => parametri.append(nome, valore))
  }
  const query = parametri.toString()
  return apiGet(`/mazzi${query ? `?${query}` : ''}`, signal)
}

// I valori possibili dei filtri della lista mazzi.
// Risposta attesa:
// {
//   "scuola": ["Guerrieri", "Maghi", "Furfanti"],
//   "eco": ["Dominio", "Sapienza", "Inganno", "Oblio"],
//   "ordina": ["Più recenti", "Nome (A-Z)", "Punti"]   // nel Figma sono ancora "Opzione"
// }
export function getFiltriMazzi(signal) {
  return apiGet('/mazzi/filtri', signal)
}

// Un mazzo da modificare.
// Risposta attesa:
// { "id": 4, "nome": "Mazzo del tramonto", "echi": ["Sapienza", "Oblio"],
//   "carte": [ { "cartaId": 12, "copie": 2 } ] }
export function getMazzo(id, signal) {
  return apiGet(`/mazzi/${id}`, signal)
}

// Le carte che POSSIEDI delle 2 Eco scelte (per costruire il mazzo).
//   /mazzi/carte-disponibili?eco=Sapienza&eco=Oblio
// Risposta attesa:
// [ { "id": 12, "nome": "Specchio", "immagine": "https://...", "eco": "Sapienza",
//     "punti": 2, "scuola": "Maghi", "rarita": "Comune", "abilita": "Riflesso",
//     "quantita": 3 } ]                        // quantita = copie che possiedi
export function getCarteDisponibili(echi, signal) {
  const parametri = new URLSearchParams(echi.map((eco) => ['eco', eco]))
  return apiGet(`/mazzi/carte-disponibili?${parametri}`, signal)
}

// Salva il mazzo: nuovo (POST) oppure modificato (PUT).
// Invia: { "nome": "...", "echi": ["Sapienza", "Oblio"], "carte": [ { "cartaId": 12, "copie": 2 } ] }
// Risposta: { "id": 4 }
// ⚠️ Il backend deve ricontrollare le regole (30 carte, 98 punti, 3 copie, 2 Echi).
export function salvaMazzo(mazzo, id) {
  return id ? apiPut(`/mazzi/${id}`, mazzo) : apiPost('/mazzi', mazzo)
}
