import { apiGet, apiPost } from './client'

/**
 * Richieste ricevute.
 * ✅ COLLEGATO (solo amicizia): GET /private/friendships/requests/received
 *   → [{ id, requesterUsername, status, requestedAt, ... }]
 * ⚠️ Le richieste di SFIDA non ci sono ancora nel backend.
 * Restituisce: [ { "id": 5, "tipo": "amicizia", "da": { "nome": "@kael" }, "ricevutaIl": "2026-11-22T21:28:00" } ]
 * @param {AbortSignal} signal
 */
export async function getRichieste(signal) {
  const ricevute = (await apiGet('/private/friendships/requests/received', signal)) ?? []
  return ricevute
    .filter((r) => r.status === 'PENDING')
    .map((r) => ({
      id: r.id,
      tipo: 'amicizia',
      da: { nome: `@${r.requesterUsername}` },
      ricevutaIl: r.requestedAt,
    }))
}

/**
 * ⚠️ NON ANCORA NEL BACKEND: accetta una richiesta.
 * Per una SFIDA invia anche il mazzo: { "mazzoId": 1 } e risponde { "partitaId": 42 }
 */
export function accettaRichiesta(id, mazzoId) {
  return apiPost(`/richieste/${id}/accetta`, mazzoId ? { mazzoId } : undefined)
}

/** ⚠️ NON ANCORA NEL BACKEND: rifiuta una richiesta. */
export function rifiutaRichiesta(id) {
  return apiPost(`/richieste/${id}/rifiuta`)
}
