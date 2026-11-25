// Filtri delle carte nell'editor del mazzo.
// Qui il filtro lo facciamo nel browser: le carte delle 2 Eco sono già tutte scaricate.

export const FASCE_COSTO = [
  { etichetta: '1–3 punti', min: 1, max: 3 },
  { etichetta: '4–6 punti', min: 4, max: 6 },
  { etichetta: '7–10 punti', min: 7, max: 10 },
]

export const NESSUN_FILTRO = { scuola: [], rarita: [], costo: [], abilita: [] }

// Tiene solo le carte che rispettano TUTTI i filtri scelti
export function filtraCarte(carte, filtri) {
  return carte.filter((carta) => {
    if (filtri.scuola.length && !filtri.scuola.includes(carta.scuola)) return false
    if (filtri.rarita.length && !filtri.rarita.includes(carta.rarita)) return false
    if (filtri.abilita.length && !filtri.abilita.includes(carta.abilita)) return false
    if (filtri.costo.length) {
      const nellaFascia = FASCE_COSTO.some(
        (fascia) => filtri.costo.includes(fascia.etichetta) && carta.punti >= fascia.min && carta.punti <= fascia.max,
      )
      if (!nellaFascia) return false
    }
    return true
  })
}

export function quantiFiltri(filtri) {
  return Object.values(filtri).reduce((somma, valori) => somma + valori.length, 0)
}
