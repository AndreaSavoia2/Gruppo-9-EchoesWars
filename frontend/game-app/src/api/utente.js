import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorso e forma dei dati.
//
// Risposta attesa:
// {
//   "nome": "Flavio",
//   "crediti": 1250,
//   "avatar": "https://.../libro.png",  // immagine dell'avatar scelto (null = icona standard)
//   "primoAccesso": true                // true = mostra le 3 schermate di benvenuto
// }
export function getUtente(signal) {
  return apiGet('/utente', signal)
}

// ⚠️ DA CONFERMARE col backend.
// Segna che l'utente ha visto (o saltato) le schermate di benvenuto,
// così la volta dopo "primoAccesso" arriva false.
// Invia: niente   Risposta: niente (204)
export function segnaBenvenutoVisto() {
  return apiPost('/utente/benvenuto')
}
