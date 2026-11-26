import { Link } from 'react-router'
import CardImage from '../card/CardImage'

/**
 * Collection card (from the Figma "App/Carta"): image + badge with the copies (×2, ×3).
 * The badge only appears if you have more than one.
 *
 * @param {object} props - the component props
 * @param {object} props.card
 * @returns {JSX.Element}
 */
export default function CollectionCard({ card }) {
  return (
    <Link to={`/collection/card/${card.id}`} className="relative block aspect-[107/150] w-full">
      <CardImage card={card} className="size-full" />
      {card.amount > 1 && (
        <span className="absolute -top-1.5 right-1 rounded-full border-[1.5px] border-background bg-ink px-2 py-0.5 text-caption leading-[1.4] font-medium text-background lg:-top-2 lg:text-[16.8px]">
          ×{card.amount}
          <span className="sr-only"> copie</span>
        </span>
      )}
    </Link>
  )
}
