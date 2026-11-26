import { LinkButton } from '../Button'

// How every outcome looks (from the Figma G5 / G5b)
const outcomes = {
  victory: {
    title: 'Hai vinto!',
    icon: '🏆',
    label: 'Vittoria',
    styleName: 'border-success bg-success-background text-success',
  },
  // Not in the Figma: same structure, neutral grey colour (it can happen: same HP at the end of the match)
  draw: {
    title: 'Pareggio',
    icon: '🤝',
    label: 'Pareggio',
    styleName: 'border-neutral bg-background-secondary text-neutral',
  },
  defeat: {
    title: 'Hai perso',
    icon: '✕',
    label: 'Sconfitta',
    styleName: 'border-error bg-error-background text-error',
  },
}

/**
 * G5 / G5b / DG4 / DG4b · Match result
 *
 * @param {object} props - the component props
 * @param {object} props.match
 * @returns {JSX.Element}
 */
export default function MatchResult({ match }) {
  const outcome = outcomes[match.outcome]

  const numbers = [
    { value: `${match.myHp} – ${match.opponentHp}`, label: 'PV finali' },
    { value: `+${match.creditsEarned}`, label: 'Crediti' },
    { value: match.round, label: 'Round' },
  ]

  return (
    <div className="flex flex-col items-center gap-4 pt-2 lg:items-start lg:gap-6 lg:pt-0">
      <div className="flex flex-col items-center gap-4 lg:items-start lg:gap-1.5">
        <h1 className="font-title text-h2">{outcome.title}</h1>
        <p>
          contro {match.opponent.name} · {match.round} round
        </p>
      </div>

      <div className="flex w-full flex-col items-center gap-4 lg:flex-row lg:gap-6">
        {/* Circle with the outcome: icon + word (not only the colour) */}
        <div
          className={`flex size-30 shrink-0 flex-col items-center justify-center rounded-full border-3 lg:size-35 ${outcome.styleName}`}
        >
          <span aria-hidden="true" className="font-title text-[28px]">
            {outcome.icon}
          </span>
          <span className="sr-only lg:not-sr-only lg:leading-[1.4] lg:font-semibold">{outcome.label}</span>
        </div>

        {/* 3 boxes with the numbers */}
        <dl className="flex w-full gap-3 lg:flex-1 lg:gap-4">
          {numbers.map((n) => (
            <div
              key={n.label}
              className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-[14px] border border-gold bg-background px-2 py-3 lg:rounded-2xl lg:p-6"
            >
              <dt className="text-caption leading-[1.4] font-medium">{n.label}</dt>
              <dd className="font-title text-h2 lg:text-[36px]">{n.value}</dd>
            </div>
          ))}
        </dl>
      </div>

      <div className="flex w-full flex-col gap-4 lg:w-auto lg:flex-row">
        <LinkButton to={`/match/${match.id}/details`} className="w-full lg:min-h-12 lg:w-50">
          Vedi i dettagli
        </LinkButton>
        <LinkButton to="/" variant="secondary" className="w-full lg:min-h-12 lg:w-50">
          Torna alla home
        </LinkButton>
      </div>
    </div>
  )
}
