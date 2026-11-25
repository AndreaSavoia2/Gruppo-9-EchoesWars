import { cancellaToken, leggiToken, salvaToken, vaiAlLogin } from './token'

// Indirizzo del backend, preso dal file .env (vedi .env.example)
// Esempio: http://localhost:8080/api/v1  → i percorsi sono tipo "/auth/me", "/private/decks"
const API_URL = import.meta.env.VITE_API_URL

/**
 * Funzione base per TUTTE le richieste al backend.
 * - aggiunge il token (Authorization: Bearer ...)
 * - manda i cookie (credentials: 'include'): servono per il rinnovo del token
 * - se il token è scaduto (401) prova a rinnovarlo UNA volta e ripete la richiesta
 * - "scarta l'incarto": il backend risponde { timestamp, httpStatus, message, data }
 *   e alle pagine restituiamo solo "data"
 * @param {string} metodo - 'GET', 'POST', 'PUT', 'DELETE'
 * @param {string} percorso - es. "/private/decks"
 * @param {{ corpo?: object, signal?: AbortSignal, riprova?: boolean }} [opzioni]
 * @returns {Promise<any>} i dati della risposta
 */
async function richiesta(metodo, percorso, { corpo, signal, riprova = true } = {}) {
  // 🧪 Modalità demo (npm run demo): risponde il backend finto di demo.js.
  // import() = il file demo viene caricato SOLO in demo, non finisce nell'app vera.
  if (import.meta.env.VITE_DEMO === 'true') {
    const { rispostaDemo } = await import('./demo')
    return rispostaDemo(metodo, percorso, corpo)
  }

  if (!API_URL) {
    throw new Error('Manca VITE_API_URL nel file .env')
  }

  const token = leggiToken()
  let risposta
  try {
    risposta = await fetch(`${API_URL}${percorso}`, {
      method: metodo,
      signal,
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        ...(corpo && { 'Content-Type': 'application/json' }),
        // 🔑 Il "braccialetto": su OGNI richiesta diciamo al backend chi siamo
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: corpo ? JSON.stringify(corpo) : undefined,
    })
  } catch (errore) {
    // fetch "esplode" (senza risposta) quando il server è spento o non c'è rete.
    // L'annullamento (AbortError) invece lo lasciamo passare così com'è.
    if (errore.name === 'AbortError') throw errore
    throw creaErrore(`Server non raggiungibile (${percorso})`, 'rete')
  }

  // 401 = token mancante, falso o scaduto.
  // Il token dura solo 15 minuti: proviamo a rinnovarlo e ripetiamo la richiesta.
  // Se non si può (es. sono passati 7 giorni) → si torna al login statico.
  if (risposta.status === 401 && !PERCORSI_LOGIN.includes(percorso)) {
    if (riprova && (await rinnovaToken())) {
      return richiesta(metodo, percorso, { corpo, signal, riprova: false })
    }
    cancellaToken()
    vaiAlLogin('scaduto')
  }

  const json = await leggiJson(risposta)

  // ok è false per gli errori (400, 404, 500...)
  // 500 e oltre = problema del server → tipo 'server'
  if (!risposta.ok) {
    const errore = creaErrore(
      `Errore ${risposta.status} su ${percorso}`,
      risposta.status >= 500 ? 'server' : 'richiesta',
      risposta.status,
    )
    errore.messaggio = json?.message ?? null // testo del backend, es. "Deck name already exists."
    errore.campi = json?.errors ?? null // errori per campo, es. { "name": "must not be blank" }
    throw errore
  }

  return scartaIncarto(json)
}

/** Legge il corpo della risposta come JSON (null se è vuoto o non è JSON). */
async function leggiJson(risposta) {
  if (risposta.status === 204) return null
  try {
    return await risposta.json()
  } catch {
    return null
  }
}

/**
 * Il backend "incarta" i dati: { timestamp, httpStatus, message, data }.
 * Se la risposta ha questa forma restituisce solo "data", altrimenti la lascia com'è.
 */
function scartaIncarto(json) {
  if (json && typeof json === 'object' && 'httpStatus' in json && 'data' in json) return json.data
  return json
}

// Le richieste del login stesso: se rispondono 401 NON si prova a rinnovare il token
const PERCORSI_LOGIN = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/csrf', '/auth/logout']

// ---------- Rinnovo del token ----------
// Il backend, al login, salva un "refresh token" in un cookie nascosto (valido 7 giorni).
// Con quello si chiede un nuovo token, protetti da un codice CSRF:
//   1. GET  /auth/csrf     → { token, headerName }
//   2. POST /auth/refresh  con l'header X-XSRF-TOKEN → { accessToken }
// Se più richieste scadono insieme, il rinnovo si fa UNA volta sola (rinnovoInCorso).
let rinnovoInCorso = null

/**
 * Chiede al backend un nuovo token usando il cookie di rinnovo.
 * @returns {Promise<boolean>} true se il token è stato rinnovato
 */
function rinnovaToken() {
  if (!rinnovoInCorso) {
    rinnovoInCorso = (async () => {
      try {
        const opzioni = { credentials: 'include', headers: { Accept: 'application/json' } }
        const csrf = scartaIncarto(await leggiJson(await fetch(`${API_URL}/auth/csrf`, opzioni)))
        if (!csrf?.token) return false
        const risposta = await fetch(`${API_URL}/auth/refresh`, {
          ...opzioni,
          method: 'POST',
          headers: { ...opzioni.headers, [csrf.headerName ?? 'X-XSRF-TOKEN']: csrf.token },
        })
        if (!risposta.ok) return false
        const dati = scartaIncarto(await leggiJson(risposta))
        if (!dati?.accessToken) return false
        salvaToken(dati.accessToken)
        return true
      } catch {
        return false
      } finally {
        rinnovoInCorso = null
      }
    })()
  }
  return rinnovoInCorso
}

/**
 * Errore con due informazioni in più, utili per decidere cosa mostrare:
 *   tipo:  'rete' (server spento / niente internet), 'server' (errore 500...),
 *          'richiesta' (errore 400, 404...)
 *   stato: il codice HTTP (404, 500...)
 */
function creaErrore(messaggio, tipo, stato) {
  const errore = new Error(messaggio)
  errore.tipo = tipo
  errore.stato = stato
  return errore
}

/** Leggere dati: apiGet('/private/decks') */
export function apiGet(percorso, signal) {
  return richiesta('GET', percorso, { signal })
}

/** Inviare dati: apiPost('/private/decks', { name: 'Nuovo' }) */
export function apiPost(percorso, corpo) {
  return richiesta('POST', percorso, { corpo })
}

/** Modificare dati che esistono già: apiPut('/private/decks/4', { name: 'Nuovo nome' }) */
export function apiPut(percorso, corpo) {
  return richiesta('PUT', percorso, { corpo })
}

/** Eliminare: apiDelete('/private/decks/4') */
export function apiDelete(percorso) {
  return richiesta('DELETE', percorso)
}
