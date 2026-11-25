import CartaGioco from './CartaGioco'

/**
 * Mostra una carta in qualunque formato arrivi dal backend:
 *   - immagine intera già pronta (stringa, o oggetto con "immagine") → la mostra come prima
 *   - altrimenti, oggetto con i valori (atk, def, eco...) → la disegna a strati con <CartaGioco>
 * Così le pagine funzionano sia con i dati vecchi sia con quelli nuovi.
 * className: le misure (es. "h-43.5 w-31"). Per le posizioni (absolute, rotate...)
 * avvolgi in un <div>.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.carta
 * @param {string} [props.className='']
 * @returns {JSX.Element}
 */
export default function ImmagineCarta({ carta, className = '' }) {
  if (carta && typeof carta === 'object' && !carta.immagine && carta.atk !== undefined) {
    return <CartaGioco carta={carta} className={className} />
  }
  const src = typeof carta === 'string' ? carta : carta?.immagine
  const alt = typeof carta === 'object' ? (carta?.nome ?? '') : ''
  return <img src={src} alt={alt} className={`rounded-lg object-cover ${className}`} />
}
