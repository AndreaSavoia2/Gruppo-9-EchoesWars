/**
 * Iniziali da mostrare nel cerchio dell'avatar.
 * "@lyra_eco" → "LE"   ·   "Mario Rossi" → "MR"   ·   "ombra" → "OM"
 *
 * @param {string} [nome='']
 */
export function iniziali(nome = '') {
  const parole = nome.replace('@', '').split(/[\s_.-]+/).filter(Boolean)
  if (parole.length >= 2) return (parole[0][0] + parole[1][0]).toUpperCase()
  return (parole[0] ?? '').slice(0, 2).toUpperCase()
}

/**
 * Data leggibile: "Oggi, 21:30" · "Ieri, 18:05" · "12 nov, 09:10"
 *
 * @param {*} dataIso
 */
export function dataLeggibile(dataIso) {
  if (!dataIso) return ''
  const data = new Date(dataIso)
  const ora = data.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })

  const oggi = new Date()
  const ieri = new Date()
  ieri.setDate(oggi.getDate() - 1)
  const stessoGiorno = (a, b) => a.toDateString() === b.toDateString()

  if (stessoGiorno(data, oggi)) return `Oggi, ${ora}`
  if (stessoGiorno(data, ieri)) return `Ieri, ${ora}`
  return `${data.toLocaleDateString('it-IT', { day: 'numeric', month: 'short' })}, ${ora}`
}

/**
 * Quanto tempo fa: "oggi" · "ieri" · "3 giorni fa" · "1 settimana fa" · "2 mesi fa"
 *
 * @param {*} dataIso
 */
export function tempoFa(dataIso) {
  if (!dataIso) return null
  const inizioGiorno = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const giorni = Math.round((inizioGiorno(new Date()) - inizioGiorno(new Date(dataIso))) / 86_400_000)

  if (giorni <= 0) return 'oggi'
  if (giorni === 1) return 'ieri'
  if (giorni < 7) return `${giorni} giorni fa`
  const settimane = Math.floor(giorni / 7)
  if (settimane < 5) return settimane === 1 ? '1 settimana fa' : `${settimane} settimane fa`
  const mesi = Math.floor(giorni / 30)
  return mesi <= 1 ? '1 mese fa' : `${mesi} mesi fa`
}

/**
 * Tempo che manca a una data: "20 h" · "45 min" · "47 h 59 min" (conMinuti = true)
 *
 * @param {*} dataIso
 * @param {boolean} [conMinuti=false]
 */
export function tempoMancante(dataIso, conMinuti = false) {
  const minuti = Math.max(0, Math.round((new Date(dataIso) - Date.now()) / 60_000))
  const ore = Math.floor(minuti / 60)
  if (ore === 0) return `${minuti} min`
  return conMinuti ? `${ore} h ${minuti % 60} min` : `${ore} h`
}

/**
 * Data breve: "3 novembre"
 *
 * @param {*} dataIso
 */
export function dataBreve(dataIso) {
  return new Date(dataIso).toLocaleDateString('it-IT', { day: 'numeric', month: 'long' })
}

/**
 * Da quanto tempo, per le richieste: "adesso" · "2 min fa" · "1 ora fa" · "5 ore fa" · poi come tempoFa ("ieri"...)
 *
 * @param {*} dataIso
 */
export function quantoFa(dataIso) {
  const minuti = Math.max(0, Math.round((Date.now() - new Date(dataIso)) / 60_000))
  if (minuti < 1) return 'adesso'
  if (minuti < 60) return `${minuti} min fa`
  const ore = Math.floor(minuti / 60)
  if (ore < 24 && new Date(dataIso).toDateString() === new Date().toDateString()) return ore === 1 ? '1 ora fa' : `${ore} ore fa`
  return tempoFa(dataIso)
}
