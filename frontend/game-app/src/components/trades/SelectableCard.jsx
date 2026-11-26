import { rarityColor } from '../../data/rarity'

const reasons = { deck: '🔒 In mazzo', reserved: '⏳ Riservata' }

/**
 * Card to choose in the "Nuova proposta" (new proposal). Locked = dark veil + reason, it cannot be tapped.
 *
 * @param {object} props - the component props
 * @param {object} props.card
 * @param {boolean} props.choice - true if it is the chosen card
 * @param {Function} props.onChoose
 * @returns {JSX.Element}
 */
export default function SelectableCard({ card, choice, onChoose }) {
  const locked = Boolean(card.locked)
  return (
    <button
      type="button"
      onClick={onChoose}
      disabled={locked}
      aria-pressed={choice}
      className="flex w-full flex-col items-start gap-1 text-left text-caption leading-[1.4] font-medium disabled:cursor-not-allowed"
    >
      <span className={`relative block aspect-[107/118] w-full overflow-hidden rounded-lg ${choice ? 'border-3 border-ink' : ''}`}>
        {/* Cropped thumbnail: shows only the illustration (or the old complete image) */}
        <img src={card.illustration ?? card.image} alt="" className="size-full object-cover" />
        {choice && (
          <span className="absolute top-0.75 left-0.75 rounded-full border border-ink bg-gold px-2 py-0.75">✓ Scelta</span>
        )}
        {locked && (
          <>
            <span className="absolute inset-0 bg-[rgba(33,38,43,0.55)]" />
            <span className="absolute bottom-1.5 left-1.5 rounded-lg bg-background px-1.5 py-0.75">{reasons[card.locked]}</span>
          </>
        )}
      </span>
      <span>{card.name}</span>
      <span className={rarityColor[card.rarity]}>
        <span aria-hidden="true">◆ </span>
        {card.rarity}
      </span>
    </button>
  )
}
