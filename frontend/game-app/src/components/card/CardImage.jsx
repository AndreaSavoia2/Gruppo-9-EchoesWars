import GameCard from './GameCard'

/**
 * Shows a card in whatever format it comes from the backend:
 *   - complete ready-made image (string, or object with "image") → shows it as before
 *   - otherwise, object with the values (atk, def, echo...) → draws it in layers with <GameCard>
 * This way the pages work both with the old data and with the new data.
 * className: the sizes (e.g. "h-43.5 w-31"). For positions (absolute, rotate...)
 * wrap it in a <div>.
 *
 * @param {object} props - the component props
 * @param {object|string} props.card
 * @param {string} [props.className='']
 * @returns {JSX.Element}
 */
export default function CardImage({ card, className = '' }) {
  if (card && typeof card === 'object' && !card.image && card.atk !== undefined) {
    return <GameCard card={card} className={className} />
  }
  const src = typeof card === 'string' ? card : card?.image
  const alt = typeof card === 'object' ? (card?.name ?? '') : ''
  return <img src={src} alt={alt} className={`rounded-lg object-cover ${className}`} />
}
