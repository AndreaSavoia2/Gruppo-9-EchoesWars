import { cancellaToken, leggiToken, vaiAlLogin } from './token'

// Indirizzo del backend, preso dal file .env (vedi .env.example)
const API_URL = import.meta.env.VITE_API_URL

// Funzione base per TUTTE le richieste al backend.
// metodo: 'GET' per leggere, 'POST' per inviare
// corpo:  i dati da inviare (solo per POST)
// signal: serve per annullare la richiesta se la pagina viene chiusa
async function richiesta(metodo, percorso, { corpo, signal } = {}) {
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

  // 401 = token mancante, falso o scaduto → si torna al login statico
  if (risposta.status === 401) {
    cancellaToken()
    vaiAlLogin('scaduto')
  }

  // ok è false per gli errori (404, 500...)
  // 500 e oltre = problema del server → tipo 'server'
  if (!risposta.ok) {
    throw creaErrore(`Errore ${risposta.status} su ${percorso}`, risposta.status >= 500 ? 'server' : 'richiesta', risposta.status)
  }

  // 204 = "fatto, ma non ti mando niente indietro"
  return risposta.status === 204 ? null : risposta.json()
}

// Errore con due informazioni in più, utili per decidere cosa mostrare:
//   tipo:  'rete' (server spento / niente internet), 'server' (errore 500...),
//          'richiesta' (errore 400, 404...)
//   stato: il codice HTTP (404, 500...)
function creaErrore(messaggio, tipo, stato) {
  const errore = new Error(messaggio)
  errore.tipo = tipo
  errore.stato = stato
  return errore
}

// Leggere dati: apiGet('/mazzi')
export function apiGet(percorso, signal) {
  return richiesta('GET', percorso, { signal })
}

// Inviare dati: apiPost('/partite', { mazzoId: 3 })
export function apiPost(percorso, corpo) {
  return richiesta('POST', percorso, { corpo })
}

// Modificare dati che esistono già: apiPut('/mazzi/4', { nome: 'Nuovo nome' })
export function apiPut(percorso, corpo) {
  return richiesta('PUT', percorso, { corpo })
}

// Eliminare: apiDelete('/amici/4')
export function apiDelete(percorso) {
  return richiesta('DELETE', percorso)
}
