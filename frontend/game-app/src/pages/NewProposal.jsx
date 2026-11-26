import { useCallback, useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import Button, { LinkButton } from '../components/Button'
import SelectableRow from '../components/SelectableRow'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import CardPair from '../components/trades/CardPair'
import SelectableCard from '../components/trades/SelectableCard'
import { useFetch } from '../hooks/useFetch'
import { getTradeFriends, getFriendCards, getMyTradeCards, sendProposal } from '../api/trades'
import { TRADE_RULES } from '../data/rules'
import { shortDate } from '../utils/text'

/**
 * 10b-10f · /trades/new — a single page, 5 moments:
 *   1 friend → 2 your card → 3 the card you want → 4 summary → 5 sent
 *
 * @returns {JSX.Element}
 */
export default function NewProposal() {
  const [step, setStep] = useState(1)
  const [friend, setFriend] = useState(null)
  const [myCard, setMyCard] = useState(null)
  const [theirCard, setTheirCard] = useState(null)
  const [sending, setSending] = useState({ inProgress: false, error: null })

  /** Sends the trade proposal. */
  async function send() {
    setSending({ inProgress: true, error: null })
    try {
      await sendProposal({ friendId: friend.id, offeredCardId: myCard.id, requestedCardId: theirCard.id })
      setStep(5)
    } catch {
      setSending({ inProgress: false, error: 'Invio non riuscito. Riprova.' })
    }
  }

  // Texts of every step: title, subtitle, "‹ back"
  const texts = {
    1: ['Nuova proposta', 'Una carta contro una della stessa rarità.', 'Scambi'],
    2: ['Nuova proposta', `Con ${friend?.name}`, 'Amico'],
    3: ['Nuova proposta', `Dalla collezione di ${friend?.name}`, 'La tua carta'],
    4: ['Riepilogo', 'Controlla prima di inviare.', 'Carta richiesta'],
    5: ['Proposta inviata', null, null],
  }
  const [title, subtitle, back] = texts[step]

  return (
    <AppLayout activePage="trades">
      <div className="flex flex-col gap-2.5 lg:max-w-3xl lg:gap-4">
        <PageHeader title={title} subtitle={subtitle} />

        {back &&
          (step === 1 ? (
            <Link to="/trades" className="mt-4 flex min-h-11 items-center self-start font-medium text-ink-secondary lg:mt-0">
              ‹ {back}
            </Link>
          ) : (
            <button type="button" onClick={() => setStep(step - 1)} className="mt-4 flex min-h-11 items-center self-start font-medium text-ink-secondary lg:mt-0">
              ‹ {back}
            </button>
          ))}

        {step <= 3 && <Progress step={step} />}

        {step === 1 && (
          <FriendStep
            selected={friend}
            onChoose={(picked) => {
              // Change friend → the chosen cards are no longer valid
              if (picked.id !== friend?.id) setTheirCard(null)
              setFriend(picked)
            }}
            onNext={() => setStep(2)}
          />
        )}
        {step === 2 && (
          <MyCardStep
            choice={myCard}
            onChoose={(card) => {
              if (card.rarity !== myCard?.rarity) setTheirCard(null)
              setMyCard(card)
            }}
            onNext={() => setStep(3)}
          />
        )}
        {step === 3 && <TheirCardStep friend={friend} rarity={myCard.rarity} choice={theirCard} onChoose={setTheirCard} onNext={() => setStep(4)} />}

        {step === 4 && (
          <>
            <div className="mt-2 rounded-2xl border border-gold-border bg-background px-4 py-3.5">
              <CardPair
                left={{ label: 'Offri', card: myCard }}
                right={{ label: `Chiedi a ${friend.name}`, card: theirCard }}
              />
            </div>
            <ul className="flex flex-col gap-2 rounded-2xl border border-gold-border bg-background px-4 py-3.5 text-caption leading-[1.4]">
              <li>
                • La proposta vale {TRADE_RULES.proposalHours} ore: se {friend.name} non risponde, scade da sola.
              </li>
              <li>• {myCard.name} resta riservata finché la proposta non viene accettata, rifiutata, annullata o scade.</li>
              <li>• Lo scambio avviene solo se {friend.name} accetta.</li>
            </ul>
            <Button onClick={send} disabled={sending.inProgress} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
              {sending.inProgress ? 'Invio…' : 'Invia proposta'}
            </Button>
            {sending.error && <ErrorMessage text={sending.error} />}
          </>
        )}

        {step === 5 && (
          <>
            <div className="mt-6 flex flex-col gap-3">
              <Notice kind="success" withBar>
                Proposta inviata a {friend.name}. Ti avvisiamo appena risponde.
              </Notice>
              <div className="rounded-2xl border border-gold-border bg-background px-4 py-3.5">
                <CardPair left={{ label: 'Offri', card: myCard }} right={{ label: 'Chiedi', card: theirCard }} />
              </div>
              <p className="text-center leading-[1.4] font-semibold">
                Scade tra {TRADE_RULES.proposalHours} ore se {friend.name} non risponde.
              </p>
            </div>
            <div className="flex flex-col gap-2.5 lg:flex-row">
              <LinkButton to="/trades?tab=sent" className="w-full lg:w-auto lg:min-w-60">
                Vedi proposte inviate
              </LinkButton>
              <LinkButton to="/trades" variant="secondary" className="w-full lg:w-auto lg:min-w-60">
                Torna agli scambi
              </LinkButton>
            </div>
          </>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * "Passo 2 di 3 · ..." (step 2 of 3) + bar in 3 pieces
 *
 * @param {object} props - the component props
 * @param {number} props.step
 * @returns {JSX.Element}
 */
function Progress({ step }) {
  const names = ['Scegli l’amico', 'Scegli la carta da offrire', 'Scegli la carta che vuoi']
  return (
    <div className="flex flex-col gap-1.5">
      <p className="leading-[1.4] font-semibold">
        Passo {step} di 3 · {names[step - 1]}
      </p>
      <div className="flex gap-1.5" aria-hidden="true">
        {[1, 2, 3].map((n) => (
          <span key={n} className={`h-1.5 flex-1 rounded-sm ${n <= step ? 'bg-gold-border' : 'bg-background-secondary'}`} />
        ))}
      </div>
    </div>
  )
}

/**
 * Step 1 of the trade proposal: choose the friend.
 *
 * @param {object} props - the component props
 * @param {object|null} props.selected
 * @param {Function} props.onChoose
 * @param {Function} props.onNext
 * @returns {JSX.Element}
 */
function FriendStep({ selected, onChoose, onNext }) {
  const { data: friends, loading, error } = useFetch(getTradeFriends)
  return (
    <>
      {loading && <Skeleton className="h-72 rounded-2xl" />}
      {error && <ErrorMessage />}
      {friends?.length === 0 && <p>Non hai ancora amici con cui scambiare. Aggiungili dalla sezione Amici.</p>}
      <ul className="mt-2 flex flex-col gap-2.5 lg:grid lg:grid-cols-2">
        {friends?.map((friend) => (
          <li key={friend.id}>
            <SelectableRow
              title={friend.name}
              detail={`Amico dal ${shortDate(friend.friendsSince)}`}
              selected={friend.id === selected?.id}
              onChoose={() => onChoose(friend)}
            />
          </li>
        ))}
      </ul>
      <Button onClick={onNext} disabled={!selected} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
        Avanti
      </Button>
    </>
  )
}

/**
 * Grid of selectable cards (only one can be chosen).
 *
 * @param {object} props - the component props
 * @param {object[]} props.cardList
 * @param {object|null} props.choice
 * @param {Function} props.onChoose
 * @returns {JSX.Element}
 */
function CardGrid({ cardList, choice, onChoose }) {
  return (
    <ul className="mt-4 grid grid-cols-3 gap-x-3 gap-y-2.5 lg:grid-cols-5">
      {cardList.map((card) => (
        <li key={card.id}>
          <SelectableCard card={card} choice={card.id === choice?.id} onChoose={() => onChoose(card)} />
        </li>
      ))}
    </ul>
  )
}

/**
 * Step 2 of the trade proposal: choose your card to offer.
 *
 * @param {object} props - the component props
 * @param {object|null} props.choice
 * @param {Function} props.onChoose
 * @param {Function} props.onNext
 * @returns {JSX.Element}
 */
function MyCardStep({ choice, onChoose, onNext }) {
  const { data: cardList, loading, error } = useFetch(getMyTradeCards)
  return (
    <>
      {loading && <Skeleton className="mt-4 h-72 rounded-2xl" />}
      {error && <ErrorMessage />}
      {cardList && <CardGrid cardList={cardList} choice={choice} onChoose={onChoose} />}
      <Button onClick={onNext} disabled={!choice} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
        Avanti
      </Button>
    </>
  )
}

/**
 * Step 3 of the trade proposal: choose the card you ask the friend for (same rarity).
 *
 * @param {object} props - the component props
 * @param {object} props.friend
 * @param {string} props.rarity
 * @param {object|null} props.choice
 * @param {Function} props.onChoose
 * @param {Function} props.onNext
 * @returns {JSX.Element}
 */
function TheirCardStep({ friend, rarity, choice, onChoose, onNext }) {
  const load = useCallback((signal) => getFriendCards(friend.id, rarity, signal), [friend.id, rarity])
  const { data: cardList, loading, error } = useFetch(load)
  return (
    <>
      {loading && <Skeleton className="mt-4 h-72 rounded-2xl" />}
      {error && <ErrorMessage />}
      {cardList?.length === 0 && (
        <p className="mt-4">
          {friend.name} non ha carte {rarity.toLowerCase()} da scambiare. Torna indietro e scegli un’altra carta.
        </p>
      )}
      {cardList?.length > 0 && <CardGrid cardList={cardList} choice={choice} onChoose={onChoose} />}
      <Button onClick={onNext} disabled={!choice} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
        Rivedi la proposta
      </Button>
    </>
  )
}
