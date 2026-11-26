import { useCallback, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Tabs from '../components/Tabs'
import Notice from '../components/Notice'
import Modal from '../components/Modal'
import Button, { LinkButton } from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import CardPair from '../components/trades/CardPair'
import { useFetch } from '../hooks/useFetch'
import { cancelProposal, getTrades, declineTrade } from '../api/trades'
import { TRADE_RULES } from '../data/rules'
import { timeLeft } from '../utils/text'

const statusLabels = { accepted: '✓ Accettato', declined: 'Rifiutato', expired: 'Scaduto', cancelled: 'Annullato' }

/**
 * 10 · 10a · 10g · 10j / D9 · /trades?tab=received|sent|history
 *
 * @returns {JSX.Element}
 */
export default function Trades() {
  const [params] = useSearchParams()
  const tab = params.get('tab') ?? 'received'
  const { data, loading, error, reload } = useFetch(getTrades)
  const [toCancel, setToCancel] = useState(null)
  const closeCancel = useCallback(() => setToCancel(null), [])

  /** Declines a received proposal. */
  async function decline(id) {
    await declineTrade(id)
    reload()
  }

  /** Cancels the proposal chosen in the confirmation window. */
  async function cancel() {
    await cancelProposal(toCancel.id)
    setToCancel(null)
    reload()
  }

  // 10a · Echo Market still locked
  if (data?.isLocked) {
    return (
      <AppLayout activePage="trades">
        <div className="flex flex-col gap-5 lg:max-w-md">
          <PageHeader title="Scambi" subtitle="Scambia carte con i tuoi amici." />
          <MarketLocked {...data.isLocked} />
        </div>
      </AppLayout>
    )
  }

  const isPaused = Boolean(data?.paused)
  const tabs = [
    { id: 'received', label: `Ricevute${data ? ` (${data.received.length})` : ''}`, path: '/trades' },
    { id: 'sent', label: `Inviate${data ? ` (${data.sent.length})` : ''}`, path: '/trades?tab=sent' },
    { id: 'history', label: 'Storico', path: '/trades?tab=history' },
  ]
  const itemList = data?.[tab] ?? []

  const newButton = (
    <LinkButton
      to="/trades/new"
      variant="secondary"
      aria-disabled={isPaused}
      onClick={(event) => isPaused && event.preventDefault()}
      className={`w-full lg:w-auto ${isPaused ? 'pointer-events-none opacity-40' : ''}`}
    >
      Nuova proposta
    </LinkButton>
  )

  return (
    <AppLayout activePage="trades">
      <div className="flex flex-col gap-3 lg:gap-6">
        <PageHeader title="Scambi" subtitle="Una carta contro una della stessa rarità." />

        <div className="flex items-center justify-between gap-6 pt-3 lg:pt-0">
          <Tabs tabs={tabs} active={tab} label="Proposte di scambio" />
          <div className="hidden lg:block">{newButton}</div>
        </div>

        <div className="flex flex-col gap-6 lg:flex-row lg:items-start">
          <div className="flex flex-1 flex-col gap-3 lg:gap-4">
            {isPaused ? (
              <Notice kind="error" icon="⏸️" withBar>
                Scambi in pausa per {TRADE_RULES.pauseHours} ore dopo lo scambio con {data.paused.withFriend}. Potrai accettare o
                proporre scambi tra {timeLeft(data.paused.endsOn, true)}. Le proposte ricevute restano qui.
              </Notice>
            ) : (
              tab === 'received' && (
                <Notice kind="info" withBar>
                  Una proposta scade dopo {TRADE_RULES.proposalHours} ore se non rispondi. Dopo uno scambio accettato gli
                  scambi si fermano per {TRADE_RULES.pauseHours} ore.
                </Notice>
              )
            )}

            {loading && <Skeleton className="h-64 rounded-2xl" />}
            {error && <ErrorMessage />}
            {data && itemList.length === 0 && <p>{emptyText[tab]}</p>}

            {itemList.length > 0 && (
              <ul className="grid gap-3 lg:grid-cols-2 lg:gap-4">
                {itemList.map((proposal) => (
                  <li key={proposal.id}>
                    {tab === 'received' && (
                      <ReceivedProposal proposal={proposal} isPaused={isPaused} onDecline={() => decline(proposal.id)} />
                    )}
                    {tab === 'sent' && <SentProposal proposal={proposal} onCancel={() => setToCancel(proposal)} />}
                    {tab === 'history' && <PastProposal proposal={proposal} />}
                  </li>
                ))}
              </ul>
            )}

            <div className="lg:hidden">{newButton}</div>
          </div>

          {/* 💻 Desktop: the rules on the right */}
          <aside className="hidden w-100 shrink-0 flex-col gap-3 rounded-2xl border border-gold bg-background p-4 lg:flex">
            <h2 className="font-title text-h2">Come funzionano gli scambi</h2>
            <ul className="flex flex-col gap-2 text-caption">
              <li>• Scambi carte solo con i tuoi amici.</li>
              <li>• 1 carta in cambio di 1 carta della stessa rarità.</li>
              <li>• Una proposta vale {TRADE_RULES.proposalHours} ore, poi scade se non rispondi.</li>
              <li>• Dopo uno scambio accettato, gli scambi si fermano per {TRADE_RULES.pauseHours} ore.</li>
              <li>• Il Mercato degli Echi si sblocca dopo {TRADE_RULES.matchesToUnlock} partite valide.</li>
            </ul>
          </aside>
        </div>
      </div>

      {toCancel && (
        <Modal title="Annullare la proposta?" onClose={closeCancel}>
          <p className="text-ink-secondary">
            La proposta a {toCancel.friend.name} verrà ritirata e {toCancel.offered.name} tornerà disponibile.
          </p>
          <Button variant="destructive" onClick={cancel}>
            Annulla proposta
          </Button>
          <Button variant="secondary" onClick={closeCancel}>
            Tieni la proposta
          </Button>
        </Modal>
      )}
    </AppLayout>
  )
}

const emptyText = {
  received: 'Nessuna proposta ricevuta per ora.',
  sent: 'Non hai proposte in attesa.',
  history: 'Non hai ancora fatto scambi.',
}

const panel = 'flex flex-col gap-2 rounded-2xl border border-gold-border bg-background px-4 py-3.5'

/**
 * Proposal you received: "Ti offre" (offers you) ⇄ "Vuole" (wants)
 *
 * @param {object} props - the component props
 * @param {object} props.proposal
 * @param {boolean} props.isPaused - true while the 48-hour pause is active
 * @param {Function} props.onDecline
 * @returns {JSX.Element}
 */
function ReceivedProposal({ proposal, isPaused, onDecline }) {
  const navigate = useNavigate()
  return (
    <article className={panel}>
      <div className="flex justify-between leading-[1.4]">
        <h3 className="font-semibold">{proposal.friend.name}</h3>
        <p className="text-caption font-medium text-ink-secondary">Scade tra {timeLeft(proposal.expiresOn)}</p>
      </div>
      <CardPair
        left={{ label: 'Ti offre', card: proposal.offered }}
        right={{ label: 'Vuole', card: proposal.request }}
        showName={false}
      />
      <div className="flex gap-2.5">
        <Button onClick={() => navigate(`/trades/${proposal.id}`)} disabled={isPaused} className="flex-1 disabled:opacity-40">
          Accetta
        </Button>
        <Button variant="secondary" onClick={onDecline} className="flex-1">
          Rifiuta
        </Button>
      </div>
    </article>
  )
}

/**
 * 10g · Proposal you sent: "Offri" (you offer) ⇄ "Chiedi" (you ask)
 *
 * @param {object} props - the component props
 * @param {object} props.proposal
 * @param {Function} props.onCancel
 * @returns {JSX.Element}
 */
function SentProposal({ proposal, onCancel }) {
  return (
    <article className="flex flex-col gap-3">
      <div className="flex justify-between leading-[1.4]">
        <h3 className="font-semibold">A {proposal.friend.name}</h3>
        <p className="text-caption font-medium text-info">⏳ In attesa · scade tra {timeLeft(proposal.expiresOn)}</p>
      </div>
      <div className={panel}>
        <CardPair left={{ label: 'Offri', card: proposal.offered }} right={{ label: 'Chiedi', card: proposal.request }} />
      </div>
      <Button variant="destructive" onClick={onCancel}>
        Annulla proposta
      </Button>
    </article>
  )
}

/**
 * A proposal already finished, in the trade history.
 *
 * @param {object} props - the component props
 * @param {object} props.proposal
 * @returns {JSX.Element}
 */
function PastProposal({ proposal }) {
  return (
    <article className={panel}>
      <div className="flex justify-between leading-[1.4]">
        <h3 className="font-semibold">{proposal.friend.name}</h3>
        <p className={`text-caption font-medium ${proposal.status === 'accepted' ? 'text-success' : 'text-ink-secondary'}`}>
          {statusLabels[proposal.status] ?? proposal.status}
        </p>
      </div>
      <CardPair left={{ label: 'Offerta', card: proposal.offered }} right={{ label: 'Richiesta', card: proposal.request }} />
    </article>
  )
}

/**
 * 10a · The market unlocks after N matches
 *
 * @param {object} props - the component props
 * @param {number} props.matches - matches played
 * @param {number} props.requests - matches required
 * @returns {JSX.Element}
 */
function MarketLocked({ matches, requests }) {
  const notches = Array.from({ length: requests }, (_, i) => i)
  return (
    <>
      <section className="flex flex-col items-center gap-3 rounded-2xl border border-gold-border bg-background px-5 py-6 text-center">
        <span aria-hidden="true" className="text-2xl">🔒</span>
        <h2 className="font-title text-[22px]">Il Mercato degli Echi si sblocca dopo {requests} partite valide</h2>
        <div className="flex w-full gap-1" aria-hidden="true">
          {notches.map((i) => (
            <span key={i} className={`h-2 flex-1 rounded ${i < matches ? 'bg-gold-border' : 'bg-background-secondary'}`} />
          ))}
        </div>
        <p className="leading-[1.4] font-semibold">
          {matches} di {requests} partite giocate. Ne mancano {requests - matches}.
        </p>
      </section>
      <LinkButton to="/play">Gioca una partita</LinkButton>
    </>
  )
}
