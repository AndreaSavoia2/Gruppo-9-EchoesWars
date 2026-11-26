// The 4 Echoes of the game (from the Figma "App/Scelta Eco"). They are fixed: they do not come from the backend.
// "color" = Tailwind classes with the "factions" colours of the theme
export const echoes = [
  { name: 'Dominio', description: 'Forza e comando', icon: '/icons/echoes/dominion.png', text: 'text-dominion', border: 'border-dominion', background: 'bg-dominion' },
  { name: 'Sapienza', description: 'Conoscenza e magia', icon: '/icons/echoes/wisdom.png', text: 'text-wisdom', border: 'border-wisdom', background: 'bg-wisdom' },
  { name: 'Inganno', description: 'Astuzia e trucchi', icon: '/icons/echoes/deception.png', text: 'text-deception', border: 'border-deception', background: 'bg-deception' },
  { name: 'Oblio', description: 'Ombra e oblio', icon: '/icons/echoes/oblivion.png', text: 'text-oblivion', border: 'border-oblivion', background: 'bg-oblivion' },
]

/**
 * Finds an Echo by name (e.g. "Sapienza").
 *
 * @param {string} name
 * @returns {object|undefined}
 */
export function findEcho(name) {
  return echoes.find((echo) => echo.name === name)
}
