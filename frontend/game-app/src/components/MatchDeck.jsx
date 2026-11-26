import { Link } from 'react-router'

/**
 * A deck (from the Figma "App/Mazzo per partita").
 * 3 states: Valid · Selected (thick border + "✓ Scelto") · Not valid (faded + 🔒)
 * - with onChoose → it is a button ("Scegli il mazzo" screen)
 * - with path → it is a link (deck list: it opens to edit it, even if not valid)
 *
 * @param {object} props - the component props
 * @param {object} props.deck
 * @param {boolean} [props.chosenOne=false]
 * @param {Function} [props.onChoose]
 * @param {string} [props.path]
 * @returns {JSX.Element}
 */
export default function MatchDeck({ deck, chosenOne = false, onChoose, path }) {
  const { name, valid, reason, points, maxPoints } = deck
  // Under the name: the points if it is valid, otherwise the reason
  const info = valid ? `${points}/${maxPoints} punti` : reason

  let styleName = 'border border-gold bg-background' // valid
  if (chosenOne) styleName = 'border-3 border-ink bg-background-secondary'
  if (!valid) styleName = 'border border-gold bg-background-secondary'

  const content = (
    <>
      {/* Pile of 3 cards: 2 backs behind + the front in front */}
      <span className={`relative block h-29 w-22 shrink-0 ${valid ? '' : 'opacity-40'}`} aria-hidden="true">
        <img src="/images/deck/back-2.png" alt="" className="absolute top-0 left-3.5 h-27 w-18 rounded-md object-cover opacity-55" />
        <img src="/images/deck/back-1.png" alt="" className="absolute top-1 left-1.5 h-27 w-18 rounded-md object-cover opacity-80" />
        <img src="/images/deck/front.png" alt="" className="absolute top-1.5 left-0 h-27 w-18 rounded-md object-cover" />
      </span>

      <span
        className={`flex min-h-24 w-full flex-1 flex-col items-center justify-center gap-0.5 rounded-[10px] px-1 py-1.5 text-center leading-[1.4] ${styleName}`}
      >
        <span className="line-clamp-2 font-semibold">{name}</span>
        {chosenOne ? (
          <span className="text-caption font-medium">✓ Scelto</span>
        ) : valid ? (
          <span className="text-caption font-medium text-success">✓ Valido</span>
        ) : (
          <span className="text-caption font-medium text-error">🔒 Non valido</span>
        )}
        <span className="w-full truncate text-caption font-medium">{info}</span>
      </span>
    </>
  )

  const classes = 'flex min-h-58 flex-col items-center gap-1.5 lg:w-35'

  if (path) {
    return (
      <Link to={path} className={classes}>
        {content}
      </Link>
    )
  }

  return (
    <button
      type="button"
      onClick={onChoose}
      disabled={!valid}
      aria-pressed={chosenOne}
      className={`${classes} disabled:cursor-not-allowed`}
    >
      {content}
    </button>
  )
}
