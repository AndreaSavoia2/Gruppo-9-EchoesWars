import { apiDelete, apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// I tuoi amici. Risposta: [ { "id": 3, "nome": "@lyra_eco" } ]
export function getAmiciLista(signal) {
  return apiGet('/amici', signal)
}

// Manda una richiesta di amicizia. Invia: { "nome": "@aurora_eco" }
// Se il nome non esiste il backend risponde con un errore (es. 404).
export function aggiungiAmico(nome) {
  return apiPost('/amici/richieste', { nome })
}

export function eliminaAmico(id) {
  return apiDelete(`/amici/${id}`)
}
