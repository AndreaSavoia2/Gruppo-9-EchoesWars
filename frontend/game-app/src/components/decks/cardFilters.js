// Card filters in the deck editor.
// Here the filtering happens in the browser: the cards of the 2 Echoes are already all downloaded.

export const COST_RANGES = [
  { label: '1–3 punti', min: 1, max: 3 },
  { label: '4–6 punti', min: 4, max: 6 },
  { label: '7–10 punti', min: 7, max: 10 },
]

export const NO_FILTERS = { school: [], rarity: [], cost: [], ability: [] }

/**
 * Keeps only the cards that match ALL the chosen filters
 *
 * @param {object[]} cardList
 * @param {{ school: string[], rarity: string[], cost: string[], ability: string[] }} filters
 * @returns {object[]}
 */
export function filterCards(cardList, filters) {
  return cardList.filter((card) => {
    if (filters.school.length && !filters.school.includes(card.school)) return false
    if (filters.rarity.length && !filters.rarity.includes(card.rarity)) return false
    if (filters.ability.length && !filters.ability.includes(card.ability)) return false
    if (filters.cost.length) {
      const inBand = COST_RANGES.some(
        (band) => filters.cost.includes(band.label) && card.points >= band.min && card.points <= band.max,
      )
      if (!inBand) return false
    }
    return true
  })
}

/**
 * Counts how many filters are active (the number on the "Filtri" button).
 *
 * @param {object} filters
 * @returns {number}
 */
export function countFilters(filters) {
  return Object.values(filters).reduce((sum, values) => sum + values.length, 0)
}
