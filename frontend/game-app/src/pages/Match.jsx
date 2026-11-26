import { useCallback, useEffect } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Simulation from '../components/match/Simulation'
import MatchResult from '../components/match/MatchResult'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import ErrorScreen from '../components/ErrorScreen'
import { LinkButton } from '../components/Button'
import { useFetch } from '../hooks/useFetch'
import { useUser } from '../context/UserContext'
import { getMatch } from '../api/matches'

// How often to ask for the status while the match is in progress
const WAIT_MS = 2000

/**
 * /match/:id → the "heart" of the Play flow:
 *   1. match just started   → "Stiamo simulando" (matchmaking)
 *   2. when it is ready     → the animated battle starts by itself (/match/:id/battle)
 *   3. battle finished      → we come back here and see "Hai vinto / Hai perso" (you won / you lost)
 * How does it know whether it has "just started"? Whoever starts the match arrives here with
 * navigate(..., { state: { isNew: true } }). If you open the link in another way
 * (e.g. "‹ Risultato" from the details) you see the result straight away, without the battle.
 *
 * @returns {JSX.Element}
 */
export default function Match() {
  // useParams reads the variable parts of the address: /match/42 → id = "42"
  const { id } = useParams()
  const loadMatch = useCallback((signal) => getMatch(id, signal), [id])
  const { data: match, loading, error, reload } = useFetch(loadMatch)
  const { reload: reloadUser } = useUser()
  const navigate = useNavigate()
  const isNew = useLocation().state?.isNew === true

  const inProgress = match?.status === 'in_progress'
  const finished = match?.status === 'finished'

  // While the match is in progress, ask for the status every 2 seconds
  useEffect(() => {
    if (!inProgress) return
    const timer = setTimeout(reload, WAIT_MS)
    return () => clearTimeout(timer)
  }, [match, inProgress, reload])

  // New match and ready → off we go with the animated battle.
  // replace = the matchmaking page does not stay in the browser history
  useEffect(() => {
    if (finished && isNew) navigate(`/match/${id}/battle`, { replace: true })
  }, [finished, isNew, navigate, id])

  // When we see the result, refresh the credits in the top bar
  useEffect(() => {
    if (finished && !isNew) reloadUser()
  }, [finished, isNew, reloadUser])

  // The connection dropped during the battle (Figma E3):
  // the backend ends the match by itself, the result will be in the history.
  if (match?.status === 'interrupted') {
    return (
      <ErrorScreen
        title="Partita interrotta"
        text="La connessione si è interrotta durante la battaglia. La partita viene simulata dal sistema: trovi il risultato nella cronologia."
        icon="⏸️"
        color="notice"
      >
        <LinkButton to="/history">Vai alla cronologia</LinkButton>
        <LinkButton to="/" variant="secondary">
          Torna alla home
        </LinkButton>
      </ErrorScreen>
    )
  }

  // Arena background on desktop only for the result (as in the Figma)
  const desktopBackground = finished && !isNew ? '/images/random-mode.png' : undefined

  return (
    <AppLayout activePage="play" mobileBackground="/images/play-background.png" desktopBackground={desktopBackground} hideMobileMenu>
      {loading && (
        <div className="flex flex-col items-center gap-4 lg:items-start">
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-4 w-72" />
        </div>
      )}
      {error && <ErrorMessage text="Impossibile caricare la partita. Riprova più tardi." />}
      {/* While we wait (or a moment before starting the battle): matchmaking */}
      {(inProgress || (finished && isNew)) && <Simulation match={match} />}
      {finished && !isNew && <MatchResult match={match} />}
    </AppLayout>
  )
}
