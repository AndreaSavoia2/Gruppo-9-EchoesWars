// 🔑 Gestione del token JWT (il "braccialetto" che dimostra chi è l'utente).
//
// Il token lo CREA il backend Spring (/api/login).
// Il login è una pagina STATICA (fatta da un'altra persona): dopo il login
// porta l'utente qui, all'app React, con il token nell'indirizzo:
//
//     http://localhost:5173/#token=eyJhbGciOi...
//
// Appena l'app parte, prendiTokenDallIndirizzo():
//   1. legge il token dall'indirizzo
//   2. lo salva nel "cassetto" del browser (localStorage)
//   3. lo CANCELLA dall'indirizzo (così non resta nella cronologia del browser)
// Il pezzo dopo "#" non viene mai mandato a nessun server: resta nel browser.
//
// Il contratto completo da dare a chi fa le pagine statiche è in docs/login-e-token.md

const CHIAVE = 'echoeswar_token' // nome del "cassetto" in localStorage

// Indirizzo della pagina di login statica (dal file .env).
// Se non c'è (es. stai sviluppando senza login), l'app non fa redirect.
const URL_LOGIN = import.meta.env.VITE_LOGIN_URL

// localStorage può dare errore (es. navigazione privata con dati bloccati): try/catch
export function leggiToken() {
  try {
    return localStorage.getItem(CHIAVE)
  } catch {
    return null
  }
}

export function salvaToken(token) {
  try {
    localStorage.setItem(CHIAVE, token)
  } catch {
    // niente da fare: il token resterà valido solo finché la pagina è aperta
  }
}

export function cancellaToken() {
  try {
    localStorage.removeItem(CHIAVE)
  } catch {
    // niente da fare
  }
}

// Da chiamare UNA volta all'avvio dell'app (in main.jsx)
export function prendiTokenDallIndirizzo() {
  // "#token=abc&altro=1" → URLSearchParams lo legge come una query
  const parametri = new URLSearchParams(window.location.hash.slice(1))
  const token = parametri.get('token')
  if (!token) return

  salvaToken(token)
  // Toglie "#token=..." dall'indirizzo senza ricaricare la pagina
  window.history.replaceState(null, '', window.location.pathname + window.location.search)
}

// Porta alla pagina di login statica.
// motivo: 'scaduto' (token non più valido) o 'uscito' (ha premuto Esci) → la pagina statica
// può mostrare un messaggio tipo "Sessione scaduta, accedi di nuovo".
export function vaiAlLogin(motivo) {
  if (!URL_LOGIN) return false // nessun login configurato: restiamo qui
  window.location.href = motivo ? `${URL_LOGIN}?motivo=${motivo}` : URL_LOGIN
  return true
}

// C'è un login configurato ma l'utente non ha il token? → va fatto il login
export function serveLogin() {
  return Boolean(URL_LOGIN) && !leggiToken()
}
