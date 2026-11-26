// 🌍 App language.
// For now there are two languages: Italian and English.
// ⚠️ The choice is saved only in the browser (localStorage): the backend has no "language" field yet.

/** The available languages: code (for the lang attribute), name in Italian and name in the language itself. */
export const LANGUAGES = [
  { code: 'it', name: 'Italiano', originalName: 'Italiano' },
  { code: 'en', name: 'Inglese', originalName: 'English' },
]

const STORAGE_KEY = 'echoeswar_language' // name of the "drawer" in localStorage

/**
 * The language chosen by the user (Italian if they never chose one).
 * @returns {{ code: string, name: string, originalName: string }}
 */
export function chosenLanguage() {
  let code = null
  try {
    code = localStorage.getItem(STORAGE_KEY)
  } catch {
    // localStorage not available (e.g. private browsing): we use Italian
  }
  return LANGUAGES.find((language) => language.code === code) ?? LANGUAGES[0]
}

/**
 * Saves the chosen language and applies it to the page.
 * @param {string} code - "it" or "en"
 * @returns {void}
 */
export function saveLanguage(code) {
  try {
    localStorage.setItem(STORAGE_KEY, code)
  } catch {
    // nothing to do: the choice is valid while the page stays open
  }
  applyLanguage()
}

/**
 * Writes the language into the lang attribute of the page (<html lang="it">).
 * Screen readers need it to read with the right pronunciation.
 * @returns {void}
 */
export function applyLanguage() {
  document.documentElement.lang = chosenLanguage().code
}
