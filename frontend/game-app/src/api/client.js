import { clearToken, readToken, saveToken, goToLogin } from './token'

// Backend address, taken from the .env file (see .env.example)
// Example: http://localhost:8080/api/v1  → paths look like "/auth/me", "/private/decks"
const API_URL = import.meta.env.VITE_API_URL

/**
 * Base function for ALL the requests to the backend.
 * - adds the token (Authorization: Bearer ...)
 * - sends the cookies (credentials: 'include'): they are needed to refresh the token
 * - if the token has expired (401) it tries to refresh it ONCE and repeats the request
 * - "unwraps the envelope": the backend answers { timestamp, httpStatus, message, data }
 *   and the pages only get "data"
 * @param {string} method - 'GET', 'POST', 'PUT', 'DELETE'
 * @param {string} path - e.g. "/private/decks"
 * @param {object} [options]
 * @param {object} [options.body] - data to send as JSON
 * @param {AbortSignal} [options.signal] - to cancel the request
 * @param {boolean} [options.retry=true] - false after a token refresh, to avoid loops
 * @returns {Promise<any>} the response data
 */
async function request(method, path, { body, signal, retry = true } = {}) {
  // 🧪 Demo mode (npm run demo): the fake backend in demo.js answers.
  // import() = the demo file is loaded ONLY in demo mode, it never ends up in the real app.
  if (import.meta.env.VITE_DEMO === 'true') {
    const { demoResponse } = await import('./demo')
    return demoResponse(method, path, body)
  }

  if (!API_URL) {
    throw new Error('Manca VITE_API_URL nel file .env')
  }

  const token = readToken()
  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: method,
      signal,
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        ...(body && { 'Content-Type': 'application/json' }),
        // 🔑 The "wristband": on EVERY request we tell the backend who we are
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch (error) {
    // fetch "blows up" (no response) when the server is off or there is no network.
    // A cancellation (AbortError) is passed on as it is.
    if (error.name === 'AbortError') throw error
    throw makeError(`Server non raggiungibile (${path})`, 'network')
  }

  // 401 = token missing, fake or expired.
  // The token only lasts 15 minutes: we try to refresh it and repeat the request.
  // If that is not possible (e.g. 7 days have passed) → back to the static login page.
  if (response.status === 401 && !AUTH_PATHS.includes(path)) {
    if (retry && (await refreshToken())) {
      return request(method, path, { body, signal, retry: false })
    }
    clearToken()
    goToLogin('scaduto')
  }

  const json = await readJson(response)

  // ok is false for errors (400, 404, 500...)
  // 500 and above = server problem → kind 'server'
  if (!response.ok) {
    const error = makeError(
      `Errore ${response.status} su ${path}`,
      response.status >= 500 ? 'server' : 'request',
      response.status,
    )
    error.message = json?.message ?? null // backend text, e.g. "Deck name already exists."
    error.fields = json?.errors ?? null // errors per field, e.g. { "name": "must not be blank" }
    throw error
  }

  return unwrapEnvelope(json)
}

/**
 * Reads the response body as JSON (null if it is empty or not JSON).
 * @param {Response} response
 * @returns {Promise<any>}
 */
async function readJson(response) {
  if (response.status === 204) return null
  try {
    return await response.json()
  } catch {
    return null
  }
}

/**
 * The backend "wraps" the data: { timestamp, httpStatus, message, data }.
 * If the response has this shape it returns only "data", otherwise it leaves it as it is.
 * @param {any} json
 * @returns {any}
 */
function unwrapEnvelope(json) {
  if (json && typeof json === 'object' && 'httpStatus' in json && 'data' in json) return json.data
  return json
}

// The login requests themselves: if they answer 401 we do NOT try to refresh the token
// (change password too: there 401 means "wrong current password", not "token expired")
const AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/csrf', '/auth/logout', '/auth/change-password']

// ---------- Token refresh ----------
// At login the backend stores a "refresh token" in a hidden cookie (valid for 7 days).
// With it we ask for a new token, protected by a CSRF code:
//   1. GET  /auth/csrf     → { token, headerName }
//   2. POST /auth/refresh  with the X-XSRF-TOKEN header → { accessToken }
// If several requests expire together, the refresh happens only ONCE (refreshInProgress).
let refreshInProgress = null

/**
 * Asks the backend for a new token using the refresh cookie.
 * @returns {Promise<boolean>} true if the token was refreshed
 */
function refreshToken() {
  if (!refreshInProgress) {
    refreshInProgress = (async () => {
      try {
        const options = { credentials: 'include', headers: { Accept: 'application/json' } }
        const csrf = unwrapEnvelope(await readJson(await fetch(`${API_URL}/auth/csrf`, options)))
        if (!csrf?.token) return false
        const response = await fetch(`${API_URL}/auth/refresh`, {
          ...options,
          method: 'POST',
          headers: { ...options.headers, [csrf.headerName ?? 'X-XSRF-TOKEN']: csrf.token },
        })
        if (!response.ok) return false
        const data = unwrapEnvelope(await readJson(response))
        if (!data?.accessToken) return false
        saveToken(data.accessToken)
        return true
      } catch {
        return false
      } finally {
        refreshInProgress = null
      }
    })()
  }
  return refreshInProgress
}

/**
 * Error with two extra pieces of information, useful to decide what to show:
 *   kind:   'network' (server off / no internet), 'server' (error 500...),
 *           'request' (error 400, 404...)
 *   status: the HTTP code (404, 500...)
 * @param {string} message
 * @param {string} kind
 * @param {number} [status]
 * @returns {Error}
 */
function makeError(message, kind, status) {
  const error = new Error(message)
  error.kind = kind
  error.status = status
  return error
}

/**
 * Read data: apiGet('/private/decks')
 * @param {string} path
 * @param {AbortSignal} [signal]
 * @returns {Promise<any>}
 */
export function apiGet(path, signal) {
  return request('GET', path, { signal })
}

/**
 * Send data: apiPost('/private/decks', { name: 'New' })
 * @param {string} path
 * @param {object} [body]
 * @returns {Promise<any>}
 */
export function apiPost(path, body) {
  return request('POST', path, { body })
}

/**
 * Change data that already exists: apiPut('/private/decks/4', { name: 'New name' })
 * @param {string} path
 * @param {object} [body]
 * @returns {Promise<any>}
 */
export function apiPut(path, body) {
  return request('PUT', path, { body })
}

/**
 * Delete: apiDelete('/private/decks/4')
 * @param {string} path
 * @returns {Promise<any>}
 */
export function apiDelete(path) {
  return request('DELETE', path)
}
