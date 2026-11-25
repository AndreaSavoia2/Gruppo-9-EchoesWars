import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.

// Richieste ricevute (amicizia e sfide).
// Risposta: [ { "id": 5, "tipo": "sfida", "da": { "nome": "@kael" }, "ricevutaIl": "2026-11-22T21:28:00Z" } ]
//            tipo = "amicizia" oppure "sfida"
export function getRichieste(signal) {
  return apiGet('/richieste', signal)
}

// Accetta. Per una SFIDA invia anche il mazzo: { "mazzoId": 1 } e risponde { "partitaId": 42 }
export function accettaRichiesta(id, mazzoId) {
  return apiPost(`/richieste/${id}/accetta`, mazzoId ? { mazzoId } : undefined)
}

export function rifiutaRichiesta(id) {
  return apiPost(`/richieste/${id}/rifiuta`)
}
