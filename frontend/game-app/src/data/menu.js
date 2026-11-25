// Le 5 voci del menu: le scriviamo UNA volta sola
// e le usano sia la barra laterale (desktop) che il menu in basso (mobile).
// "percorso" = l'indirizzo della pagina (es. /collezione)
export const vociMenu = [
  { id: 'gioca', etichetta: 'Gioca', icona: '/icons/gioca.png', percorso: '/' },
  { id: 'collezione', etichetta: 'Collezione', icona: '/icons/collezione.svg', percorso: '/collezione' },
  { id: 'negozio', etichetta: 'Negozio', icona: '/icons/negozio.svg', percorso: '/negozio' },
  { id: 'scambi', etichetta: 'Scambi', icona: '/icons/scambio.svg', percorso: '/scambi' },
  { id: 'amici', etichetta: 'Amici', icona: '/icons/amici.svg', percorso: '/amici' },
]

// Su mobile "Gioca" sta al centro: qui scriviamo solo l'ordine.
export const ordineMobile = ['negozio', 'collezione', 'gioca', 'scambi', 'amici']
