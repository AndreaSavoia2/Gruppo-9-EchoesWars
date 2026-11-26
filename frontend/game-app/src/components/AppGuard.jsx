import { useEffect, useRef, useState } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useUser } from '../context/UserContext'
import { useOnline } from '../hooks/useOnline'
import ErrorScreen from './ErrorScreen'
import Button, { LinkButton } from './Button'

/**
 * Checks that apply to ALL the pages, before showing them:
 *   1. no internet               → "Sei offline" screen (E1)
 *   2. the server does not answer → "Qualcosa non ha funzionato" screen (E2)
 *      (we find out from the user request, which starts on every page)
 *   3. user's first login        → goes to the welcome screens (/welcome)
 * If everything is fine it simply shows the page (children).
 *
 * @param {object} props - the component props
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function AppGuard({ children }) {
  const { online, recheck } = useOnline()
  const { data: user, error, reload, welcomeSeen } = useUser()
  const { pathname } = useLocation()
  // true after a "Riprova" (retry) that failed: we show one more message
  const [stillOffline, setStillOffline] = useState(false)

  // When the network comes back, download the user data again (it failed while offline)
  const wasOffline = useRef(false)
  useEffect(() => {
    if (online && wasOffline.current) {
      reload()
      setStillOffline(false)
    }
    wasOffline.current = !online
  }, [online, reload])

  if (!online) {
    const retry = () => {
      recheck()
      if (navigator.onLine) reload()
      else setStillOffline(true)
    }
    return (
      <ErrorScreen
        title="Sei offline"
        text="Controlla la connessione a internet e riprova. La tua collezione e i tuoi mazzi sono salvati sul tuo account."
        icon="📶"
        color="info"
      >
        {stillOffline && <p className="text-caption text-ink-secondary">Ancora nessuna connessione.</p>}
        <Button onClick={retry}>Riprova</Button>
      </ErrorScreen>
    )
  }

  // error.kind comes from api/client.js: 'network' = server off, 'server' = error 500...
  if (error?.kind === 'network' || error?.kind === 'server') {
    return (
      <ErrorScreen
        title="Qualcosa non ha funzionato"
        text="Non riusciamo a raggiungere il server. Riprova tra qualche minuto."
        icon="⚠️"
        color="error"
      >
        <Button onClick={reload}>Riprova</Button>
        <LinkButton to="/" variant="secondary" onClick={reload}>
          Torna alla home
        </LinkButton>
      </ErrorScreen>
    )
  }

  // First login: every page leads to the welcome until it is completed
  if (user?.firstLogin && !welcomeSeen && pathname !== '/welcome') {
    return <Navigate to="/welcome" replace />
  }

  return children
}
