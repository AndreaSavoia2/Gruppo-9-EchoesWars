import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import PlayerSide from '../components/replay/PlayerSide'
import CardImage from '../components/card/CardImage'
import { useFetch } from '../hooks/useFetch'
import { useUser } from '../context/UserContext'
import { getReplay } from '../api/matches'
import '../components/replay/battle.css'

// Style of the Pause / x2 / Skip buttons (like the Exit button)
const CONTROL_CLASSES =
  'flex min-h-11 items-center justify-center rounded-full border-[1.5px] border-ink bg-background px-3 font-medium hover:bg-background-secondary'

// How long every moment of the round lasts (in milliseconds)
const DURATION = { cards: 1500, clash: 1500, outcome: 2000 }

/**
 * G7 / G7b / G7c / DG6 · automatic full-screen battle. It is used in 2 places:
 *   /match/:id/battle → right after matchmaking (way="battle")
 *       end → "Hai vinto / Hai perso" screen   ·   Exit → Home
 *   /match/:id/replay → "Rivedi partita" from the details (way="replay")
 *       end → back to the details              ·   Exit → back to the details
 *
 * @param {object} props - the component props
 * @param {'battle'|'replay'} [props.way='replay']
 * @returns {JSX.Element}
 */
export default function Replay({ way = 'replay' }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const loadReplay = useCallback((signal) => getReplay(id, signal), [id])
  const { data: replay, loading, error } = useFetch(loadReplay)
  const { data: user } = useUser()

  // Where we are: which round (index) and which moment
  const [index, setIndex] = useState(0)
  const [moment, setMoment] = useState('cards') // 'cards' → 'clash' → 'outcome'
  // 8 · Controls: pause and speed (1 normal, 2 double)
  const [paused, setPaused] = useState(false)
  const [speed, setSpeed] = useState(1)

  const details = `/match/${id}/details`
  const end = way === 'battle' ? `/match/${id}` : details
  const exitTarget = way === 'battle' ? '/' : details
  const round = replay?.round[index]

  // Moves the replay forward by itself, one moment at a time
  useEffect(() => {
    if (!round || paused) return
    const timer = setTimeout(() => {
      if (moment === 'cards') setMoment('clash')
      else if (moment === 'clash') setMoment('outcome')
      else if (index < replay.round.length - 1) {
        setIndex(index + 1)
        setMoment('cards')
      } else {
        // finished! replace = the "back" key does not restart the battle
        navigate(end, { replace: true })
      }
    }, DURATION[moment] / speed)
    return () => clearTimeout(timer)
  }, [round, moment, index, replay, navigate, end, paused, speed])

  // Esc key = like the "Esci" (exit) button
  useEffect(() => {
    const onKey = (event) => event.key === 'Escape' && navigate(exitTarget, { replace: true })
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [navigate, exitTarget])

  // In the "outcome" moment we show the situation at the END of the round
  const status = moment === 'outcome' ? round?.after : round
  const inClash = moment === 'clash'

  // What to animate for every side ("you" or "opponent"):
  //   cards   → the card "enters" (flies from the deck) if in the previous round the place was empty
  //   outcome → the DEF drops from the one at the start of the round, the destroyed card flies to the discards,
  //             the heart pulses if the HP dropped
  const previous = index > 0 ? replay?.round[index - 1].after : null
  /**
   * The animations of one side in this moment of the round.
   * @param {'you'|'opponent'} who - which side
   * @returns {object} props to pass to <PlayerSide>
   */
  function animations(who) {
    if (!round) return {}
    if (moment === 'cards') return { entering: Boolean(round[who].card) && !previous?.[who].card }
    if (moment === 'outcome') {
      return {
        previousDef: round[who].card?.def,
        destroyed: !round.after[who].card && round[who].card ? round[who].card : null,
        previousHp: round[who].hp,
      }
    }
    return {}
  }

  return (
    <main
      className={`relative isolate flex min-h-screen flex-col items-center justify-between overflow-hidden bg-ink py-2 lg:justify-center lg:gap-4 lg:py-8 ${
        inClash ? 'battle-shake' : ''
      } ${paused ? 'battle-paused' : ''}`}
      style={{ '--battle-speed': speed }}
    >
      {/* Darkened arena background */}
      <picture aria-hidden="true">
        <source media="(min-width: 1024px)" srcSet="/images/random-mode.png" />
        <img src="/images/play-background.png" alt="" className="absolute inset-0 -z-10 size-full object-cover opacity-40" />
      </picture>

      {/* Button to exit: in battle it goes to the Home, in the replay to the details.
          The result stays in the History anyway. */}
      {/* replace = with the browser "back" key you do not go back into the battle */}
      <Link
        to={exitTarget}
        replace
        className="absolute top-1.5 left-3 z-10 flex min-h-11 w-20 items-center justify-center rounded-full border-[1.5px] border-ink bg-background font-medium hover:bg-background-secondary lg:top-6 lg:left-6"
      >
        Esci
      </Link>

      {/* 8 · Controls: Pause · Speed · Skip to the end */}
      {round && (
        <div className="absolute top-1.5 right-3 z-10 flex gap-2 lg:top-6 lg:right-6">
          <button type="button" onClick={() => setPaused(!paused)} aria-pressed={paused} className={CONTROL_CLASSES}>
            {paused ? '▶ Riprendi' : '❚❚ Pausa'}
          </button>
          <button
            type="button"
            onClick={() => setSpeed(speed === 1 ? 2 : 1)}
            aria-pressed={speed === 2}
            aria-label={speed === 1 ? 'Velocità doppia' : 'Velocità normale'}
            className={CONTROL_CLASSES}
          >
            {speed === 1 ? 'x2' : 'x1'}
          </button>
          <button type="button" onClick={() => navigate(end, { replace: true })} className={CONTROL_CLASSES}>
            Salta
          </button>
        </div>
      )}

      {loading && <Skeleton className="m-auto h-40 w-72 rounded-2xl" />}
      {error && (
        <div className="m-auto rounded-2xl bg-background p-4">
          <ErrorMessage text="Impossibile caricare il replay." />
        </div>
      )}

      {status && (
        <>
          <div className="mt-12 lg:mt-0">
            <PlayerSide
              name={replay.opponent.name}
              status={status.opponent}
              atTop
              hideCard={inClash}
              speed={speed}
              {...animations('opponent')}
            />
          </div>

          {/* Middle: round number, clash or outcome message */}
          <div className="relative flex flex-1 items-center justify-center lg:flex-none" aria-live="polite">
            {inClash ? (
              <Clash round={round} />
            ) : moment === 'outcome' && round.outcome ? (
              <div className="battle-outcome rounded-2xl bg-background px-5 py-3 text-center leading-[1.4]">
                <p className="font-semibold">{round.outcome.title}</p>
                <p className="text-caption font-medium text-error">{round.outcome.detail}</p>
              </div>
            ) : (
              <p
                key={round.count}
                className="battle-pop rounded-full border-[1.5px] border-gold-border bg-gold px-3.5 py-1.5 leading-[1.4] font-semibold lg:border-2 lg:px-5 lg:py-2 lg:text-[22px]">
                Round {round.count} / {replay.roundMax}
              </p>
            )}
          </div>

          <PlayerSide
            name={`Tu · ${user?.name ?? ''}`}
            status={status.you}
            hideCard={inClash}
            speed={speed}
            {...animations('you')}
          />

          <p className="mt-2 hidden rounded-full bg-background px-4.5 py-2.5 text-caption leading-[1.4] font-medium lg:block">
            La battaglia è automatica: guarda come si scontrano le carte.
          </p>
        </>
      )}
    </main>
  )
}

/**
 * The two cards clash in the middle, with the damage taken
 *
 * @param {object} props - the component props
 * @param {object} props.round
 * @returns {JSX.Element}
 */
function Clash({ round }) {
  const { opponent, you } = round
  return (
    <div className="relative h-72 w-56">
      {/* 2 · Attack: the cards dash towards each other · 3 · Impact: flash in the middle */}
      <div className="battle-attack-down absolute top-0 right-4 rotate-6">
        <CardImage card={opponent.card} className="h-44 w-31" />
      </div>
      <div className="battle-attack-up absolute bottom-0 left-4 -rotate-6">
        <CardImage card={you.card} className="h-44 w-31" />
      </div>
      <div className="battle-flash" aria-hidden="true" />
      {round.opponentDamage > 0 && <Damage value={round.opponentDamage} className="-top-2 -right-6" />}
      {round.yourDamage > 0 && <Damage value={round.yourDamage} className="bottom-6 -left-8" />}
    </div>
  )
}

/**
 * Red number of the damage taken during the clash (e.g. −3).
 *
 * @param {object} props - the component props
 * @param {number} props.value
 * @param {string} props.className
 * @returns {JSX.Element}
 */
function Damage({ value, className }) {
  return (
    <span
      className={`battle-damage absolute font-title text-[40px] text-error [text-shadow:0_0_2px_#fff9f0,0_0_2px_#fff9f0] ${className}`}
    >
      −{value}
    </span>
  )
}
