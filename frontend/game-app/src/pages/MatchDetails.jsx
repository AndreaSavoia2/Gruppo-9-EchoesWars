import { useCallback } from 'react'
import { Link, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import { LinkButton } from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { getMatch } from '../api/matches'
import { readableDate, initials } from '../utils/text'

const outcomes = {
  victory: { text: 'Vittoria', icon: '/icons/victory.svg', emoji: '🏆', styleName: 'border-success bg-success-background text-success' },
  draw: { text: 'Pareggio', icon: '/icons/draw.svg', emoji: '🤝', styleName: 'border-neutral bg-background-secondary text-neutral' },
  defeat: { text: 'Sconfitta', icon: '/icons/defeat.svg', emoji: '✕', styleName: 'border-error bg-error-background text-error' },
}

/**
 * G6 / DG5 · /match/:id/details
 *
 * @returns {JSX.Element}
 */
export default function MatchDetails() {
  const { id } = useParams()
  const loadMatch = useCallback((signal) => getMatch(id, signal), [id])
  const { data: match, loading, error } = useFetch(loadMatch)

  return (
    <AppLayout activePage="play" mobileBackground="/images/play-background.png">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <PageHeader title="Dettaglio partita" subtitle={match ? readableDate(match.playedOn) : ''}>
          <Link to={`/match/${id}`} className="hidden font-medium underline lg:block">
            ‹ Risultato
          </Link>
        </PageHeader>

        {/* 📱 Mobile: "‹ Risultato" under the title, dark gold colour */}
        <Link to={`/match/${id}`} className="mt-2 flex min-h-11 items-center self-start font-medium text-ink-secondary lg:hidden">
          ‹ Risultato
        </Link>

        {loading && <Skeleton className="h-96 rounded-2xl" />}
        {error && <ErrorMessage text="Impossibile caricare la partita. Riprova più tardi." />}
        {match && <Content match={match} />}
      </div>
    </AppLayout>
  )
}

/**
 * Content of the match details: outcome, players, stats and buttons.
 *
 * @param {object} props - the component props
 * @param {object} props.match
 * @returns {JSX.Element}
 */
function Content({ match }) {
  const outcome = outcomes[match.outcome]
  const { opponent } = match

  // Table rows: label + value
  const stats = [
    ['Round giocati', `${match.round} / ${match.roundMax}`],
    ['PV finali', `${match.myHp} – ${match.opponentHp}`],
    ['Carte rimaste', `${match.myCardsLeft} – ${match.opponentCardsLeft}`],
    ['Come è finita', match.endReason],
    ['Crediti guadagnati', `+${match.creditsEarned}`],
  ]

  return (
    <>
      <div className="flex flex-col gap-2.5 lg:flex-row lg:items-start lg:gap-6">
        {/* Outcome + the two players */}
        <section className="flex flex-col items-center gap-2.5 rounded-2xl border border-gold-border bg-background px-4 py-3.5 lg:w-105 lg:gap-3 lg:border-gold lg:p-6">
          <p className={`flex items-center gap-1.5 rounded-full border-[1.5px] px-3.5 py-1.5 leading-[1.4] font-semibold lg:border ${outcome.styleName}`}>
            <img src={outcome.icon} alt="" className="size-4.5 lg:hidden" />
            <span aria-hidden="true" className="hidden lg:inline">{outcome.emoji}</span>
            {outcome.text}
          </p>
          <div className="flex items-start gap-2 lg:items-center lg:gap-6">
            <Player initials="TU" name="Tu" deck={match.myDeck} />
            <span className="mt-3 font-title text-[28px] text-ink-secondary lg:mt-0 lg:text-[36px] lg:text-ink">VS</span>
            <Player initials={initials(opponent.name)} name={opponent.name} deck={opponent.deck} />
          </div>
        </section>

        {/* Stats: <dl> = list of "name → value" */}
        <section className="flex flex-col rounded-2xl border border-gold-border bg-background px-4 pt-3 pb-1.5 lg:flex-1 lg:gap-2.5 lg:border-gold lg:p-6">
          <p className="text-caption lg:leading-[1.4] lg:font-medium">Valori: Tu – {opponent.name}</p>
          <dl>
            {stats.map(([label, value]) => (
              <div
                key={label}
                className="flex items-center justify-between border-b-[0.5px] border-gold-border py-2 last:border-b-0 lg:border-b-0 lg:py-1"
              >
                <dt>{label}</dt>
                <dd className="leading-[1.4] font-semibold">{value}</dd>
              </div>
            ))}
          </dl>
        </section>
      </div>

      <div className="flex flex-col gap-2.5 lg:flex-row lg:gap-4">
        <LinkButton to={`/match/${match.id}/replay`} className="w-full lg:min-h-12 lg:w-50">
          ▶ Rivedi partita
        </LinkButton>
        <LinkButton to="/" variant="secondary" className="w-full lg:min-h-12 lg:w-50">
          Torna alla home
        </LinkButton>
      </div>
    </>
  )
}

/**
 * A player in the "VS" box: initials, name and deck used.
 *
 * @param {object} props - the component props
 * @param {string} props.initials
 * @param {string} props.name
 * @param {string} props.deck
 * @returns {JSX.Element}
 */
function Player({ initials, name, deck }) {
  return (
    <div className="flex w-35 flex-col items-center gap-1 text-center leading-[1.4] lg:w-auto">
      <span className="flex size-16 items-center justify-center rounded-full border-2 border-gold-border bg-gold font-title text-2xl lg:size-18 lg:border lg:border-gold lg:bg-background-secondary lg:text-h2">
        {initials}
      </span>
      <span className="font-semibold">{name}</span>
      <span className="text-caption font-medium">{deck}</span>
    </div>
  )
}
