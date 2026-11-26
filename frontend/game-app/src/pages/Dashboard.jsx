import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Chip from '../components/Chip'
import PlayButton from '../components/PlayButton'
import LegendaryProgress from '../components/LegendaryProgress'
import Panel from '../components/Panel'
import MatchRow from '../components/MatchRow'
import MissionRow from '../components/MissionRow'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useUser } from '../context/UserContext'
import { useFetch } from '../hooks/useFetch'
import { getDashboard } from '../api/dashboard'

/**
 * App Home: greeting, shortcuts, PLAY, recent matches and Echo Trials.
 *
 * @returns {JSX.Element}
 */
export default function Dashboard() {
  const { data: user, loading: userLoading } = useUser()
  const { data, loading, error } = useFetch(getDashboard)

  return (
    <AppLayout activePage="play">
      <div className="flex flex-1 flex-col gap-5 lg:gap-6">
        {/* Header: the name comes from the backend */}
        <div className="flex flex-col gap-1 lg:gap-1.5">
          {userLoading ? (
            <Skeleton className="h-8 w-64" />
          ) : (
            <h1 className="font-title text-h2">Ciao{user ? `, ${user.name}` : ''}</h1>
          )}
          <p>La tua prossima sfida ti aspetta.</p>
        </div>

        {/* Shortcuts: the dot on "Richieste" (requests) only appears if there are new requests */}
        <div className="flex flex-wrap justify-center gap-2.5 lg:justify-start lg:gap-3">
          <Chip text="Richieste" path="/requests" notification={data?.newRequests > 0} />
          <Chip text="Missioni" path="/missions" />
          <Chip text="Cronologia" path="/history" />
        </div>

        {/* Grid: on mobile only PLAY, on desktop PLAY + right column */}
        <div className="flex flex-1 items-start gap-6 lg:items-stretch">
          <PlayButton className="flex-1" />

          <aside className="hidden w-[440px] shrink-0 flex-col gap-4 lg:flex">
            {loading && <LoadingColumn />}
            {error && <ErrorMessage />}
            {data && <DataColumn data={data} />}
          </aside>
        </div>
      </div>
    </AppLayout>
  )
}

/**
 * Right column once the data has arrived
 *
 * @param {object} props - the component props
 * @param {object} props.data
 * @returns {JSX.Element}
 */
function DataColumn({ data }) {
  const { legendary, recentMatches, trials } = data

  return (
    <>
      {legendary && <LegendaryProgress packs={legendary.packs} total={legendary.total} />}

      <Panel title="Ultime partite">
        {/* null = the backend has no matches yet */}
        {!recentMatches ? (
          <p className="text-ink-secondary">Presto disponibile.</p>
        ) : recentMatches.length === 0 ? (
          <p>Non hai ancora giocato partite.</p>
        ) : (
          recentMatches.map((match) => (
            <MatchRow
              key={match.id}
              outcome={match.outcome}
              opponent={match.opponent}
              round={match.round}
            />
          ))
        )}
        <Link to="/history" className="font-medium underline">
          Vedi tutta la cronologia
        </Link>
      </Panel>

      <Panel title={trials ? `Prove degli Echi · ${trials.completedCount}/${trials.totals}` : 'Prove degli Echi'}>
        {/* null = the backend has no Echo Trials yet */}
        {!trials && <p className="text-ink-secondary">Presto disponibile.</p>}
        {trials?.highlighted.map((trial) => (
          <MissionRow
            key={trial.id}
            title={trial.title}
            reward={trial.reward}
            progress={trial.progress}
            total={trial.total}
          />
        ))}
        <Link to="/missions" className="font-medium underline">
          Vedi tutte le Prove
        </Link>
      </Panel>
    </>
  )
}

/**
 * Right column while we wait for the backend: pulsing rectangles
 *
 * @returns {JSX.Element}
 */
function LoadingColumn() {
  return (
    <>
      <span className="sr-only">Caricamento in corso…</span>
      <Skeleton className="h-24 rounded-2xl" />
      <Skeleton className="h-72 rounded-2xl" />
      <Skeleton className="h-52 rounded-2xl" />
    </>
  )
}
