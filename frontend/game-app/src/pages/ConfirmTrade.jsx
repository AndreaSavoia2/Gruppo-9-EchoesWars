import { useCallback, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import Button, { LinkButton } from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import CardPair from '../components/trades/CardPair'
import { useFetch } from '../hooks/useFetch'
import { acceptTrade, getTrade, declineTrade } from '../api/trades'
import { TRADE_RULES } from '../data/rules'

/**
 * 10h · 10i · /trades/:id — confirmation of a received proposal, then "Scambio completato"
 *
 * @returns {JSX.Element}
 */
export default function ConfirmTrade() {
  const { id } = useParams()
  const navigate = useNavigate()
  const load = useCallback((signal) => getTrade(id, signal), [id])
  const { data: proposal, loading, error } = useFetch(load)
  const [done, setDone] = useState(false)
  const [action, setAction] = useState({ inProgress: false, error: null })

  /** Runs an action (accept / decline) and then the "after" step. */
  async function run(fn, after) {
    setAction({ inProgress: true, error: null })
    try {
      await fn(id)
      after()
    } catch {
      setAction({ inProgress: false, error: 'Operazione non riuscita. Riprova.' })
    }
  }

  return (
    <AppLayout activePage="trades">
      <div className="flex flex-col gap-3 lg:max-w-2xl lg:gap-4">
        {loading && <Skeleton className="h-80 rounded-2xl" />}
        {error && <ErrorMessage text="Impossibile caricare la proposta." />}

        {proposal && !done && (
          <>
            <PageHeader title="Conferma scambio" subtitle={`Proposta di ${proposal.friend.name}`} />
            <div className="mt-6 flex flex-col gap-3">
              <Notice kind="info" withBar>
                Dopo questo scambio dovrai aspettare {TRADE_RULES.pauseHours} ore per il prossimo.
              </Notice>
              <div className="rounded-2xl border border-gold-border bg-background px-4 py-3.5">
                <CardPair
                  left={{ label: 'Ricevi', card: proposal.offered, align: 'start' }}
                  right={{ label: 'Dai', card: proposal.request, align: 'end' }}
                  compact
                />
              </div>
              <div className="flex gap-2.5">
                <Button onClick={() => run(acceptTrade, () => setDone(true))} disabled={action.inProgress} className="flex-1">
                  Accetta scambio
                </Button>
                <Button variant="secondary" onClick={() => run(declineTrade, () => navigate('/trades'))} disabled={action.inProgress} className="flex-1">
                  Rifiuta
                </Button>
              </div>
              {action.error && <ErrorMessage text={action.error} />}
            </div>
          </>
        )}

        {proposal && done && (
          <>
            <PageHeader title="Scambio completato" subtitle="Le carte sono nelle vostre collezioni." />
            <div className="mt-6 flex flex-col gap-3" aria-live="polite">
              <Notice kind="success" withBar>
                Scambio completato. Gli scambi si fermano per {TRADE_RULES.pauseHours} ore: potrai farne un altro dopo.
              </Notice>
              <div className="rounded-2xl border border-gold-border bg-background px-4 py-3.5">
                <CardPair
                  left={{ label: 'Ora è tua', card: proposal.offered, align: 'start' }}
                  right={{ label: `Ora è di ${proposal.friend.name}`, card: proposal.request, align: 'end' }}
                  symbol="✓"
                  compact
                />
              </div>
              <LinkButton to="/trades">Torna agli scambi</LinkButton>
            </div>
          </>
        )}
      </div>
    </AppLayout>
  )
}
