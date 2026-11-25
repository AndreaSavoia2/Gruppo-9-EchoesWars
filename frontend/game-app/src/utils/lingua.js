// 🌍 Lingua dell'app.
// Per ora le lingue sono due: italiano e inglese.
// ⚠️ La scelta è salvata solo nel browser (localStorage): il backend non ha ancora un campo "lingua".

/** Le lingue disponibili: codice (per l'attributo lang), nome in italiano e nome nella lingua stessa. */
export const LINGUE = [
  { codice: 'it', nome: 'Italiano', nomeOriginale: 'Italiano' },
  { codice: 'en', nome: 'Inglese', nomeOriginale: 'English' },
]

const CHIAVE = 'echoeswar_lingua' // nome del "cassetto" in localStorage

/**
 * La lingua scelta dall'utente (italiano se non ha mai scelto).
 * @returns {{ codice: string, nome: string, nomeOriginale: string }}
 */
export function linguaScelta() {
  let codice = null
  try {
    codice = localStorage.getItem(CHIAVE)
  } catch {
    // localStorage non disponibile (es. navigazione privata): usiamo l'italiano
  }
  return LINGUE.find((lingua) => lingua.codice === codice) ?? LINGUE[0]
}

/**
 * Salva la lingua scelta e la applica alla pagina.
 * @param {string} codice - "it" oppure "en"
 */
export function salvaLingua(codice) {
  try {
    localStorage.setItem(CHIAVE, codice)
  } catch {
    // niente da fare: la scelta vale finché la pagina resta aperta
  }
  applicaLingua()
}

/**
 * Scrive la lingua nell'attributo lang della pagina (<html lang="it">).
 * Serve agli screen reader per leggere con la pronuncia giusta.
 */
export function applicaLingua() {
  document.documentElement.lang = linguaScelta().codice
}
