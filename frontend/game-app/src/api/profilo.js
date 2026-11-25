import { cancellaToken } from './token'
import { apiGet, apiPut } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// Risposta attesa:
// {
//   "nome": "@NomeUtente", "email": "nome@email.it",
//   "partite": 48, "vittorie": 31, "carte": 124,
//   "lingua": "Italiano",
//   "accessi": { "giorno": 12, "totale": 30, "prossimaRicompensa": { "giorno": 15, "nome": "una cover per carte" } }
// }
export function getProfilo(signal) {
  return apiGet('/profilo', signal)
}

// Gli avatar. Risposta: [ { "id": 3, "nome": "Libro", "immagine": "https://...",   // immagine può essere null
//                          "disponibile": true, "sblocco": null, "inUso": true } ]
//   sblocco = come si sblocca, se non disponibile (es. "Giorno 15 di accesso")
export function getAvatar(signal) {
  return apiGet('/profilo/avatar', signal)
}

// Invia: { "avatarId": 3 }
export function salvaAvatar(avatarId) {
  return apiPut('/profilo/avatar', { avatarId })
}

// Esci dall'account. Con i JWT il backend non deve fare niente:
// basta "buttare via il braccialetto", cioè cancellare il token dal browser.
export function esci() {
  cancellaToken()
}
