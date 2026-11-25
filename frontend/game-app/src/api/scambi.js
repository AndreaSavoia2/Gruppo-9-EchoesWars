import { apiGet, apiPost } from './client'

// ⚠️ DA CONFERMARE col backend: percorsi e forma dei dati.
//
// Una "carta" negli scambi:  { "id": 1, "nome": "Velo Oscuro", "immagine": "https://...", "rarita": "Rara" }
// Una "proposta":
// {
//   "id": 7,
//   "amico": { "id": 3, "nome": "@ombra_sette" },
//   "offerta":   carta,   // la carta che dà CHI HA FATTO la proposta
//   "richiesta": carta,   // la carta che chiede in cambio
//   "scadeIl": "2026-11-23T18:00:00Z",
//   "stato": "in_attesa"   // in_attesa | accettata | rifiutata | scaduta | annullata
// }

/**
 * Tutto quello che serve alla pagina Scambi.
 * Risposta attesa:
 * {
 *   "bloccato": { "partite": 7, "richieste": 10 },             // null se il mercato è aperto
 *   "pausa": { "fineIl": "2026-11-24T21:00:00Z", "conAmico": "@kael" },  // null se non in pausa
 *   "ricevute": [proposta], "inviate": [proposta], "storico": [proposta]
 * }
 *
 * @param {AbortSignal} signal
 */
export function getScambi(signal) {
  return apiGet('/scambi', signal)
}

/**
 * Una proposta (per la pagina "Conferma scambio")
 *
 * @param {string|number} id
 * @param {AbortSignal} signal
 */
export function getScambio(id, signal) {
  return apiGet(`/scambi/${id}`, signal)
}

/**
 * Amici con cui scambiare. Risposta: [ { "id": 3, "nome": "@lyra_eco", "amiciDal": "2026-11-03T00:00:00Z" } ]
 *
 * @param {AbortSignal} signal
 */
export function getAmiciScambio(signal) {
  return apiGet('/scambi/amici', signal)
}

/**
 * Le TUE carte che puoi offrire.
 * Risposta: [ { ...carta, "bloccata": null } ]   bloccata = null | "mazzo" | "riservata"
 *
 * @param {AbortSignal} signal
 */
export function getMieCarteScambio(signal) {
  return apiGet('/scambi/mie-carte', signal)
}

/**
 * Le carte dell'amico della stessa rarità. Esempio: /scambi/amici/3/carte?rarita=Rara
 *
 * @param {*} amicoId
 * @param {*} rarita
 * @param {AbortSignal} signal
 */
export function getCarteAmico(amicoId, rarita, signal) {
  return apiGet(`/scambi/amici/${amicoId}/carte?rarita=${encodeURIComponent(rarita)}`, signal)
}

/**
 * Invia: { "amicoId": 3, "cartaOffertaId": 1, "cartaRichiestaId": 9 }  ·  Risposta: proposta
 *
 * @param {*} dati
 */
export function inviaProposta(dati) {
  return apiPost('/scambi', dati)
}

/**
 * Accetta una proposta di scambio ricevuta.
 *
 * @param {string|number} id
 */
export function accettaScambio(id) {
  return apiPost(`/scambi/${id}/accetta`)
}
/**
 * Rifiuta una proposta di scambio ricevuta.
 *
 * @param {string|number} id
 */
export function rifiutaScambio(id) {
  return apiPost(`/scambi/${id}/rifiuta`)
}
/**
 * Annulla una proposta di scambio che hai inviato.
 *
 * @param {string|number} id
 */
export function annullaProposta(id) {
  return apiPost(`/scambi/${id}/annulla`)
}
