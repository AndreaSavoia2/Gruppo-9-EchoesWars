import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Matchup from '../components/match/Matchup'
import Button, { LinkButton } from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { cancelChallenge, getChallenge, sendChallenge } from '../api/challenges'
import { initials } from '../utils/text'

const WAIT_MS = 2000 // how often to ask whether the friend has answered
const COUNTDOWN = 3 // seconds before starting the match

/**
 * A3 / A4 / DA3 / DA4 · /challenge/:id — waits for the friend's answer
 *
 * @returns {JSX.Element}
 */
export default function ChallengeWaiting() {
  const { id } = useParams()
  const navigate = useNavigate()
  const loadChallenge = useCallback((signal) => getChallenge(id, signal), [id])
  const { data: challenge, loading, error, reload } = useFetch(loadChallenge)
  const [cancelling, setCancelling] = useState(false)

  const pending = challenge?.status === 'pending'
  const accepted = challenge?.status === 'accepted'

  // While it is pending, ask every 2 seconds whether the friend has answered
  useEffect(() => {
    if (!pending) return
    const timer = setTimeout(reload, WAIT_MS)
    return () => clearTimeout(timer)
  }, [challenge, pending, reload])

  /** Cancels the challenge and goes back to the friend list. */
  async function cancel() {
    setCancelling(true)
    try {
      await cancelChallenge(id)
      navigate('/play/friend')
    } catch {
      setCancelling(false)
    }
  }

  return (
    <AppLayout activePage="play" mobileBackground="/images/play-background.png" hideMobileMenu>
      {loading && <Skeleton className="mx-auto h-8 w-64 lg:mx-0" />}
      {error && <ErrorMessage text="Impossibile caricare la sfida. Riprova più tardi." />}

      {pending && (
        <div className="flex flex-1 flex-col gap-6" aria-live="polite">
          <div className="flex flex-col gap-2.5 text-center lg:gap-1.5 lg:text-left">
            <h1 className="font-title text-[22px] lg:text-h2">Richiesta inviata</h1>
            <p>
              Hai scelto {challenge.myDeck}. Aspettiamo la risposta di {challenge.friend.name}
              <span className="lg:hidden">.</span>
              <span className="hidden lg:inline">
                : la richiesta scade tra <SecondsLeft expiresOn={challenge.expiresOn} /> secondi.
              </span>
            </p>
            <p className="text-caption leading-[1.4] font-medium lg:hidden">
              La richiesta scade tra <SecondsLeft expiresOn={challenge.expiresOn} /> secondi.
            </p>
          </div>

          <div className="mt-10 lg:mt-0">
            <Matchup opponentInitials={initials(challenge.friend.name)} />
          </div>

          <Button variant="secondary" onClick={cancel} disabled={cancelling} className="mt-auto min-h-12 w-full lg:mt-0 lg:w-auto lg:self-start">
            Annulla richiesta
          </Button>
        </div>
      )}

      {accepted && <ChallengeAccepted challenge={challenge} />}

      {/* 2i · 2j · Declined or expired */}
      {challenge && !pending && !accepted && <ChallengeEnded challenge={challenge} />}
    </AppLayout>
  )
}

/**
 * Shows the seconds left before the expiry and updates itself every second
 *
 * @param {object} props - the component props
 * @param {string} props.expiresOn - ISO date of the expiry
 * @returns {JSX.Element}
 */
function SecondsLeft({ expiresOn }) {
  const [seconds, setSeconds] = useState(() => secondsTo(expiresOn))

  useEffect(() => {
    const timer = setInterval(() => setSeconds(secondsTo(expiresOn)), 1000)
    return () => clearInterval(timer)
  }, [expiresOn])

  return seconds
}

/**
 * How many seconds are left until a certain date (never below zero)
 *
 * @param {string} isoDate
 * @returns {number}
 */
function secondsTo(isoDate) {
  return Math.max(0, Math.ceil((new Date(isoDate) - Date.now()) / 1000))
}

/**
 * A4 / DA4 · "@friend ha accettato!" + countdown, then the match starts
 *
 * @param {object} props - the component props
 * @param {object} props.challenge
 * @returns {JSX.Element}
 */
function ChallengeAccepted({ challenge }) {
  const navigate = useNavigate()
  const [seconds, setSeconds] = useState(COUNTDOWN)

  useEffect(() => {
    if (seconds === 0) {
      navigate(`/match/${challenge.matchId}`, { state: { isNew: true } })
      return
    }
    const timer = setTimeout(() => setSeconds(seconds - 1), 1000)
    return () => clearTimeout(timer)
  }, [seconds, navigate, challenge.matchId])

  return (
    <div className="flex flex-col gap-6" aria-live="polite">
      <div className="text-center lg:flex lg:flex-col lg:gap-1.5 lg:text-left">
        <h1 className="font-title text-[22px] lg:text-h2">{challenge.friend.name} ha accettato!</h1>
        <p className="hidden lg:block">
          {challenge.myDeck} contro {challenge.friendDeck}. La simulazione parte tra {seconds}…
        </p>
      </div>

      <div className="mt-28 lg:mt-0">
        <Matchup opponentInitials={initials(challenge.friend.name)} middle="VS" withWaves={false} opponentFaded={false} />
      </div>

      <div className="flex flex-col gap-7 text-center lg:hidden">
        <p className="whitespace-pre-wrap">{`${challenge.myDeck}  vs  ${challenge.friendDeck}`}</p>
        <p className="leading-[1.4] font-semibold">La simulazione parte tra {seconds}…</p>
      </div>
    </div>
  )
}

/**
 * 2i "Sfida rifiutata" (challenge declined) · 2j "Nessuna risposta" (no answer)
 *
 * @param {object} props - the component props
 * @param {object} props.challenge
 * @returns {JSX.Element}
 */
function ChallengeEnded({ challenge }) {
  const navigate = useNavigate()
  const declined = challenge.status === 'declined'

  /** Sends a new challenge to the same friend. */
  async function sendAgain() {
    const isNew = await sendChallenge(challenge.friend.id)
    navigate(`/challenge/${isNew.id}/deck`)
  }

  return (
    <div className="flex flex-1 flex-col gap-6" aria-live="polite">
      <div className="flex flex-col gap-2.5 text-center lg:gap-1.5 lg:text-left">
        <h1 className="font-title text-[22px] lg:text-h2">{declined ? 'Sfida rifiutata' : 'Nessuna risposta'}</h1>
        <p>
          {declined
            ? `${challenge.friend.name} non può giocare adesso. Prova più tardi o sfida un altro amico.`
            : `${challenge.friend.name} non ha risposto entro 60 secondi. La richiesta è scaduta.`}
        </p>
      </div>
      <span
        aria-hidden="true"
        className={`mx-auto mt-10 flex size-28 items-center justify-center rounded-full text-3xl lg:mx-0 lg:mt-0 ${
          declined ? 'border-2 border-error bg-error-background' : 'border-2 border-gold bg-background'
        }`}
      >
        {declined ? '✕' : '⏱'}
      </span>
      <div className="mt-auto flex flex-col gap-2.5 lg:mt-0 lg:flex-row">
        {declined ? (
          <LinkButton to="/play/friend" className="w-full lg:w-auto lg:min-w-60">
            Sfida un altro amico
          </LinkButton>
        ) : (
          <Button onClick={sendAgain} className="w-full lg:w-auto lg:min-w-60">
            Invia di nuovo
          </Button>
        )}
        <LinkButton to="/" variant="secondary" className="w-full lg:w-auto lg:min-w-60">
          Torna alla home
        </LinkButton>
      </div>
    </div>
  )
}
