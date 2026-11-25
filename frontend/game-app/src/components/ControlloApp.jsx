import { useEffect, useRef, useState } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useUtente } from '../context/UtenteContext'
import { useOnline } from '../hooks/useOnline'
import SchermataErrore from './SchermataErrore'
import Bottone, { BottoneLink } from './Bottone'

/**
 * Controlli che valgono per TUTTE le pagine, prima di mostrarle:
 *   1. niente internet           → schermata "Sei offline" (E1)
 *   2. il server non risponde    → schermata "Qualcosa non ha funzionato" (E2)
 *      (lo capiamo dalla richiesta /utente, che parte su ogni pagina)
 *   3. primo accesso dell'utente → porta alle schermate di benvenuto (/benvenuto)
 * Se va tutto bene mostra semplicemente la pagina (children).
 *
 * @param {object} props - le proprietà del componente
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function ControlloApp({ children }) {
  const { online, ricontrolla } = useOnline()
  const { dati: utente, errore, ricarica, benvenutoVisto } = useUtente()
  const { pathname } = useLocation()
  // true dopo un "Riprova" andato a vuoto: mostriamo un messaggio in più
  const [ancoraOffline, setAncoraOffline] = useState(false)

  // Quando la rete torna, riscarica i dati dell'utente (erano falliti mentre eri offline)
  const eraOffline = useRef(false)
  useEffect(() => {
    if (online && eraOffline.current) {
      ricarica()
      setAncoraOffline(false)
    }
    eraOffline.current = !online
  }, [online, ricarica])

  if (!online) {
    const riprova = () => {
      ricontrolla()
      if (navigator.onLine) ricarica()
      else setAncoraOffline(true)
    }
    return (
      <SchermataErrore
        titolo="Sei offline"
        testo="Controlla la connessione a internet e riprova. La tua collezione e i tuoi mazzi sono salvati sul tuo account."
        icona="📶"
        colore="info"
      >
        {ancoraOffline && <p className="text-didascalia text-testo-secondario">Ancora nessuna connessione.</p>}
        <Bottone onClick={riprova}>Riprova</Bottone>
      </SchermataErrore>
    )
  }

  // errore.tipo arriva da api/client.js: 'rete' = server spento, 'server' = errore 500...
  if (errore?.tipo === 'rete' || errore?.tipo === 'server') {
    return (
      <SchermataErrore
        titolo="Qualcosa non ha funzionato"
        testo="Non riusciamo a raggiungere il server. Riprova tra qualche minuto."
        icona="⚠️"
        colore="errore"
      >
        <Bottone onClick={ricarica}>Riprova</Bottone>
        <BottoneLink to="/" variante="secondario" onClick={ricarica}>
          Torna alla home
        </BottoneLink>
      </SchermataErrore>
    )
  }

  // Primo accesso: tutte le pagine portano al benvenuto finché non lo completa
  if (utente?.primoAccesso && !benvenutoVisto && pathname !== '/benvenuto') {
    return <Navigate to="/benvenuto" replace />
  }

  return children
}
