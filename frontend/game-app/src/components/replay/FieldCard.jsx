import CardImage from '../card/CardImage'
import { useCountDown } from '../../hooks/useCountDown'

/**
 * Dashed space where the played card sits, with the DEF at the bottom right.
 * Animations ("battle-" classes in battle.css):
 *   entering    → the card flies from the deck and flips (just drawn)
 *   previousDef → the DEF counts down from this number to the current one
 *   destroyed   → the card just destroyed: it goes dark and flies to the discards
 * card = null and destroyed = null → empty space
 *
 * @param {object} props - the component props
 * @param {object|null} props.card - the card on the field (with the current DEF)
 * @param {boolean} [props.entering=false] - true if it has just been drawn
 * @param {number} [props.previousDef] - the DEF before, to make it drop
 * @param {object|null} [props.destroyed=null] - the card just destroyed
 * @param {number} [props.speed=1] - 1 normal, 2 double
 * @returns {JSX.Element}
 */
export default function FieldCard({ card, entering = false, previousDef, destroyed = null, speed = 1 }) {
  const shown = card ?? destroyed
  const finalDef = card ? card.def : 0
  const { value: def, counting } = useCountDown(previousDef ?? finalDef, finalDef, speed)
  const animation = card && entering ? 'battle-draw' : !card && destroyed ? 'battle-destroyed' : ''

  return (
    <div className="relative flex h-46 w-33 items-center justify-center rounded-[10px] border-2 border-dashed border-gold-border bg-background/25">
      {shown && (
        <div className={`relative ${animation}`}>
          {animation === 'battle-draw' ? (
            // Two faces: the back is visible until the card flips
            <span className="battle-faces">
              <span className="battle-face">
                <CardImage card={shown} className="h-43.5 w-31" />
              </span>
              <span className="battle-face battle-face-back" aria-hidden="true">
                <img src="/icons/star.png" alt="" />
              </span>
            </span>
          ) : (
            <CardImage card={shown} className="h-43.5 w-31" />
          )}
          <span
            className={`absolute -right-1 -bottom-3 rounded-full bg-ink px-2 py-0.5 text-caption leading-[1.4] font-medium text-background ${
              counting ? 'battle-def-hit' : ''
            }`}
          >
            DEF {def}
          </span>
        </div>
      )}
    </div>
  )
}
