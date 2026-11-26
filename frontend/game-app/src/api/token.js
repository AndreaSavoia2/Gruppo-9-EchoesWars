// 🔑 JWT token handling (the "wristband" that proves who the user is).
//
// The token is CREATED by the Spring backend (/api/login).
// The login is a STATIC page (made by another person): after the login
// it brings the user here, to the React app, with the token in the address:
//
//     http://localhost:5173/#token=eyJhbGciOi...
//
// As soon as the app starts, takeTokenFromUrl():
//   1. reads the token from the address
//   2. saves it in the browser "drawer" (localStorage)
//   3. DELETES it from the address (so it does not stay in the browser history)
// The part after "#" is never sent to any server: it stays in the browser.
//
// The full contract for whoever builds the static pages is in docs/login-and-token.md

const STORAGE_KEY = 'echoeswar_token' // name of the "drawer" in localStorage

// Address of the static login page (from the .env file).
// If it is missing (e.g. you are developing without login), the app does not redirect.
const LOGIN_URL = import.meta.env.VITE_LOGIN_URL

// localStorage can throw (e.g. private browsing with blocked data): try/catch
/**
 * Reads the saved token.
 * @returns {string|null}
 */
export function readToken() {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

/**
 * Saves the token in localStorage.
 * @param {string} token
 * @returns {void}
 */
export function saveToken(token) {
  try {
    localStorage.setItem(STORAGE_KEY, token)
  } catch {
    // nothing to do: the token will only stay valid while the page is open
  }
}

/**
 * Deletes the token from localStorage.
 * @returns {void}
 */
export function clearToken() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    // nothing to do
  }
}

/**
 * To be called ONCE when the app starts (in main.jsx):
 * takes the token from "#token=..." in the address, saves it and removes it from the address.
 * @returns {void}
 */
export function takeTokenFromUrl() {
  // "#token=abc&other=1" → URLSearchParams reads it like a query
  const params = new URLSearchParams(window.location.hash.slice(1))
  const token = params.get('token')
  if (!token) return

  saveToken(token)
  // Removes "#token=..." from the address without reloading the page
  window.history.replaceState(null, '', window.location.pathname + window.location.search)
}

/**
 * Goes to the static login page.
 * reason: 'scaduto' (token no longer valid) or 'uscito' (pressed Log out) → the static page
 * can show a message like "Sessione scaduta, accedi di nuovo". These two values and the "motivo"
 * parameter are agreed with the static login page, so they stay in Italian.
 * @param {'scaduto'|'uscito'} reason
 * @returns {boolean} false if no login page is configured (we stay in the app)
 */
export function goToLogin(reason) {
  if (!LOGIN_URL) return false // no login configured: we stay here
  window.location.href = reason ? `${LOGIN_URL}?motivo=${reason}` : LOGIN_URL
  return true
}

/**
 * A login is configured but the user has no token? → they have to log in.
 * @returns {boolean}
 */
export function needsLogin() {
  return Boolean(LOGIN_URL) && !readToken()
}
