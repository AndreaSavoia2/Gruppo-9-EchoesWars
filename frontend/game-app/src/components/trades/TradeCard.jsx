import { rarityColor } from '../../data/rarity'
import CardImage from '../card/CardImage'

/**
 * A card inside a proposal: label above ("Ti offre"), image, name and rarity below.
 * compact = true → name and rarity on the same row ("Custode · ◆ Rara")
 *
 * @param {object} props - the component props
 * @param {string} props.label
 * @param {object} props.card
 * @param {boolean} [props.showName=true]
 * @param {boolean} [props.compact=false]
 * @param {string} [props.align='center']
 * @returns {JSX.Element}
 */
export default function TradeCard({ label, card, showName = true, compact = false, align = 'center' }) {
  const alignment = { center: 'items-center text-center', start: 'items-start', end: 'items-end text-right' }[align]
  const rarity = (
    <span className={rarityColor[card.rarity]}>
      <span aria-hidden="true">◆ </span>
      {card.rarity}
    </span>
  )
  return (
    <div className={`flex flex-col gap-1.5 text-caption leading-[1.4] font-medium ${alignment}`}>
      {label && <span className="text-ink-secondary">{label}</span>}
      <CardImage card={card} className="h-28 w-20 lg:h-24 lg:w-17" />
      {compact ? (
        <span>
          {card.name} · {rarity}
        </span>
      ) : (
        <>
          {showName && <span className="text-body font-semibold text-ink">{card.name}</span>}
          {rarity}
        </>
      )}
    </div>
  )
}
