import { Link, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Tabs from '../components/Tabs'
import Notice from '../components/Notice'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import RequestCard from '../components/friends/RequestCard'
import { useFetch } from '../hooks/useFetch'
import { useRequestAction } from '../hooks/useRequestAction'
import { getRequests } from '../api/requests'

/**
 * R1-R6 / D15 · /requests?tab=all|friendship|challenges
 *
 * @returns {JSX.Element}
 */
export default function Requests() {
  const [params] = useSearchParams()
  const tab = params.get('tab') ?? 'all'
  const { data: requests, loading, error, reload } = useFetch(getRequests)
  const { message, accept, decline } = useRequestAction(reload)

  const counting = (kind) => requests?.filter((r) => r.kind === kind).length ?? 0
  const total = requests?.length ?? 0
  const tabs = [
    { id: 'all', label: `Tutte (${total})`, path: '/requests' },
    { id: 'friendship', label: `Amicizia (${counting('friendship')})`, path: '/requests?tab=friendship' },
    { id: 'challenges', label: `Sfide (${counting('challenge')})`, path: '/requests?tab=challenges' },
  ]
  const visibleItems = requests?.filter((r) => tab === 'all' || (tab === 'challenges' ? r.kind === 'challenge' : r.kind === tab)) ?? []

  let subtitle = ' '
  if (requests) subtitle = total === 0 ? 'Nessuna richiesta in attesa' : `Hai ${total} ${total === 1 ? 'richiesta' : 'richieste'} da gestire`

  return (
    <AppLayout activePage="play">
      <div className="flex flex-col gap-2.5 lg:gap-4">
        <PageHeader title="Richieste" subtitle={subtitle}>
          <Link to="/" className="hidden font-medium underline lg:block">
            ‹ Home
          </Link>
        </PageHeader>
        <Link to="/" className="mt-3 self-start font-medium underline lg:hidden">
          ‹ Home
        </Link>

        <Tabs tabs={tabs} active={tab} label="Tipi di richiesta" className="lg:w-[480px]" />

        {message && (
          <Notice kind={message.kind} icon={message.kind === 'info' ? '' : undefined}>
            {message.text}
          </Notice>
        )}

        {loading && <Skeleton className="h-40 rounded-2xl" />}
        {error && <ErrorMessage />}

        {requests && visibleItems.length === 0 && (
          <div className="flex flex-col items-center gap-2 rounded-2xl border border-gold bg-background px-5 py-6 text-center lg:max-w-md">
            <span aria-hidden="true" className="text-2xl">📭</span>
            <h2 className="font-title text-[22px]">Nessuna richiesta</h2>
            <p>Qui trovi le richieste di amicizia e le sfide che ricevi.</p>
          </div>
        )}

        {visibleItems.length > 0 && (
          <ul className="grid gap-2.5 lg:grid-cols-2 lg:gap-3">
            {visibleItems.map((request) => (
              <li key={request.id}>
                <RequestCard request={request} onAccept={() => accept(request)} onDecline={() => decline(request)} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </AppLayout>
  )
}
