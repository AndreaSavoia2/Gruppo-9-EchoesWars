/**
 * Initials to show in the avatar circle.
 * "@lyra_eco" → "LE"   ·   "Mario Rossi" → "MR"   ·   "ombra" → "OM"
 *
 * @param {string} [name='']
 * @returns {string}
 */
export function initials(name = '') {
  const words = name.replace('@', '').split(/[\s_.-]+/).filter(Boolean)
  if (words.length >= 2) return (words[0][0] + words[1][0]).toUpperCase()
  return (words[0] ?? '').slice(0, 2).toUpperCase()
}

/**
 * Readable date (in Italian): "Oggi, 21:30" · "Ieri, 18:05" · "12 nov, 09:10"
 *
 * @param {string} isoDate
 * @returns {string}
 */
export function readableDate(isoDate) {
  if (!isoDate) return ''
  const data = new Date(isoDate)
  const now = data.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })

  const today = new Date()
  const yesterday = new Date()
  yesterday.setDate(today.getDate() - 1)
  const sameDay = (a, b) => a.toDateString() === b.toDateString()

  if (sameDay(data, today)) return `Oggi, ${now}`
  if (sameDay(data, yesterday)) return `Ieri, ${now}`
  return `${data.toLocaleDateString('it-IT', { day: 'numeric', month: 'short' })}, ${now}`
}

/**
 * How long ago (in Italian): "oggi" · "ieri" · "3 giorni fa" · "1 settimana fa" · "2 mesi fa"
 *
 * @param {string} isoDate
 * @returns {string}
 */
export function timeAgo(isoDate) {
  if (!isoDate) return null
  const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const days = Math.round((startOfDay(new Date()) - startOfDay(new Date(isoDate))) / 86_400_000)

  if (days <= 0) return 'oggi'
  if (days === 1) return 'ieri'
  if (days < 7) return `${days} giorni fa`
  const weeks = Math.floor(days / 7)
  if (weeks < 5) return weeks === 1 ? '1 settimana fa' : `${weeks} settimane fa`
  const months = Math.floor(days / 30)
  return months <= 1 ? '1 mese fa' : `${months} mesi fa`
}

/**
 * Time left until a date: "20 h" · "45 min" · "47 h 59 min" (withMinutes = true)
 *
 * @param {string} isoDate
 * @param {boolean} [withMinutes=false]
 * @returns {string}
 */
export function timeLeft(isoDate, withMinutes = false) {
  const minutes = Math.max(0, Math.round((new Date(isoDate) - Date.now()) / 60_000))
  const hours = Math.floor(minutes / 60)
  if (hours === 0) return `${minutes} min`
  return withMinutes ? `${hours} h ${minutes % 60} min` : `${hours} h`
}

/**
 * Short date (in Italian): "3 novembre"
 *
 * @param {string} isoDate
 * @returns {string}
 */
export function shortDate(isoDate) {
  return new Date(isoDate).toLocaleDateString('it-IT', { day: 'numeric', month: 'long' })
}

/**
 * How long ago, for the requests (in Italian): "adesso" · "2 min fa" · "1 ora fa" · "5 ore fa" · then like timeAgo ("ieri"...)
 *
 * @param {string} isoDate
 * @returns {string}
 */
export function howLongAgo(isoDate) {
  const minutes = Math.max(0, Math.round((Date.now() - new Date(isoDate)) / 60_000))
  if (minutes < 1) return 'adesso'
  if (minutes < 60) return `${minutes} min fa`
  const hours = Math.floor(minutes / 60)
  if (hours < 24 && new Date(isoDate).toDateString() === new Date().toDateString()) return hours === 1 ? '1 ora fa' : `${hours} ore fa`
  return timeAgo(isoDate)
}
