import FieldCard from './FieldCard'
import CardImage from '../card/CardImage'

/**
 * One side of the battlefield (from the Figma "Battaglia/Lato giocatore"):
 * pill with name + HP, and below: deck · card on the field · discards.
 * atTop = true for the opponent (the pill goes above the field)
 *
 * @param {object} props - the component props
 * @param {string} props.name
 * @param {object} props.status - snapshot of this side (hp, deckCount, discardCount, card...)
 * @param {boolean} [props.atTop] - true for the opponent
 * @param {boolean} [props.hideCard] - hides the card on the field (e.g. during the clash animation)
 * @param {boolean} [props.entering] - the card on the field has just been drawn (flies from the deck)
 * @param {number} [props.previousDef] - DEF of the card at the start of the round (to make it drop)
 * @param {object|null} [props.destroyed] - card just destroyed (flies to the discards)
 * @param {number} [props.previousHp] - HP at the start of the round (if they drop, the heart pulses)
 * @param {number} [props.speed] - 1 normal, 2 double
 * @returns {JSX.Element}
 */
export default function PlayerSide({ name, status, atTop, hideCard, entering, previousDef, destroyed, previousHp, speed }) {
  // 7 · HP lost: the heart pulses (key = hp: the animation restarts at every new value)
  const hpHit = previousHp !== undefined && status.hp < previousHp
  const pill = (
    <p className="flex items-center gap-2.5 rounded-full border-[1.5px] border-gold-border bg-gold px-3.5 py-2 leading-[1.4] font-semibold">
      {name}
      <span
        key={status.hp}
        className={`flex items-center gap-1 rounded-full bg-error px-2 py-0.5 text-background ${hpHit ? 'battle-hp-hit' : ''} ${
          status.hp <= 3 ? 'battle-hp-low' : ''
        }`}
      >
        <span aria-hidden="true" className="text-caption font-medium">♥</span>
        {status.hp} PV
      </span>
    </p>
  )

  return (
    <section aria-label={name} className="flex flex-col items-center gap-3 px-4 py-2">
      {atTop && pill}

      <div className="battle-field flex items-center justify-center gap-4.5">
        <Pile label="Mazzo" count={status.deckCount}>
          {[6, 3, 0].map((offset) => (
            <span
              key={offset}
              style={{ top: offset, left: offset }}
              className="absolute flex h-24.5 w-17.5 items-center justify-center rounded-md border-[1.5px] border-gold-border bg-background-secondary"
            >
              <img src="/icons/star.png" alt="" className="size-7.5 object-contain" />
            </span>
          ))}
        </Pile>

        <FieldCard
          card={hideCard ? null : status.card}
          entering={entering}
          previousDef={previousDef}
          destroyed={hideCard ? null : destroyed}
          speed={speed}
        />

        <Pile label="Scarti" count={status.discardCount}>
          {status.lastDiscarded && (
            <>
              {[
                [6, 'opacity-50'],
                [3, 'opacity-50'],
                [0, 'opacity-85'],
              ].map(([offset, opacity]) => (
                <div key={offset} style={{ top: offset, left: offset }} className={`absolute ${opacity}`}>
                  <CardImage card={status.lastDiscarded} className="h-24.5 w-17.5" />
                </div>
              ))}
              <span className="absolute top-0 left-0 h-24.5 w-17.5 rounded-md bg-[rgba(33,38,43,0.35)]" />
            </>
          )}
        </Pile>
      </div>

      {!atTop && pill}
    </section>
  )
}

/**
 * Pile of cards with the number in a badge at the top right
 *
 * @param {object} props - the component props
 * @param {string} props.label
 * @param {number} props.count
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
function Pile({ label, count, children }) {
  return (
    <div className="relative h-26.5 w-19">
      {children}
      <span className="absolute top-0 right-0 rounded-full border-[1.5px] border-background bg-ink px-2 py-0.5 text-caption leading-[1.4] font-medium text-background">
        <span className="sr-only">{label}: </span>
        {count}
      </span>
    </div>
  )
}
