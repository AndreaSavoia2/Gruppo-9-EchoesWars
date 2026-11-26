import { useCallback } from 'react'
import { Link, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Tabs from '../components/Tabs'
import MatchRow from '../components/MatchRow'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { getMatchHistory } from '../api/progress'

const tabs = [
  { id: 'all', label: 'Tutte', path: '/history' },
  { id: 'victory', label: 'Vittorie', path: '/history?outcome=victory' },
  { id: 'defeat', label: 'Sconfitte', path: '/history?outcome=defeat' },
  { id: 'draw', label: 'Pareggi', path: '/history?outcome=draw' },
]

/**
 * 2g / D13 · /history?outcome=victory|defeat|draw
 *
 * @returns {JSX.Element}
 */
export default function MatchHistory() {
  const [params] = useSearchParams()
  const outcome = params.get('outcome')
  const load = useCallback((signal) => getMatchHistory(outcome, signal), [outcome])
  const { data: matches, loading, error } = useFetch(load)

  return (
    <AppLayout activePage="play">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <PageHeader title="Cronologia partite" subtitle="Le tue ultime sfide." />
        <Link to="/" className="mt-4 flex min-h-11 items-center self-start font-medium text-ink-secondary lg:hidden">
          ‹ Gioca
        </Link>

        <section className="flex flex-col gap-2.5 lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-4">
          <Link to="/" className="hidden min-h-11 items-center self-start font-medium text-ink-secondary lg:flex">
            ‹ Gioca
          </Link>
          <Tabs tabs={tabs} active={outcome ?? 'all'} label="Filtra per esito" className="" />

          {loading && <Skeleton className="h-60 rounded-2xl" />}
          {error && <ErrorMessage />}
          {matches?.length === 0 && <p>Nessuna partita da mostrare.</p>}

          {matches?.length > 0 && (
            <ul className="flex flex-col gap-2.5">
              {matches.map((match) => (
                <li key={match.id}>
                  <MatchRow {...match} withVs path={`/match/${match.id}/details`} />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </AppLayout>
  )
}
