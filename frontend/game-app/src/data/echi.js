// Le 4 Eco del gioco (dal Figma "App/Scelta Eco"). Sono fisse: non arrivano dal backend.
// "colore" = classi Tailwind con i colori "fazioni" del tema
export const echi = [
  { nome: 'Dominio', descrizione: 'Forza e comando', icona: '/icons/echi/dominio.png', testo: 'text-dominio', bordo: 'border-dominio', sfondo: 'bg-dominio' },
  { nome: 'Sapienza', descrizione: 'Conoscenza e magia', icona: '/icons/echi/sapienza.png', testo: 'text-sapienza', bordo: 'border-sapienza', sfondo: 'bg-sapienza' },
  { nome: 'Inganno', descrizione: 'Astuzia e trucchi', icona: '/icons/echi/inganno.png', testo: 'text-inganno', bordo: 'border-inganno', sfondo: 'bg-inganno' },
  { nome: 'Oblio', descrizione: 'Ombra e oblio', icona: '/icons/echi/oblio.png', testo: 'text-oblio', bordo: 'border-oblio', sfondo: 'bg-oblio' },
]

/**
 * Cerca un'Eco per nome (es. "Sapienza").
 *
 * @param {string} nome
 */
export function trovaEco(nome) {
  return echi.find((eco) => eco.nome === nome)
}
