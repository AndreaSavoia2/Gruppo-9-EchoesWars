import { useCallback, useEffect } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Simulazione from '../components/partita/Simulazione'
import Risultato from '../components/partita/Risultato'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import SchermataErrore from '../components/SchermataErrore'
import { BottoneLink } from '../components/Bottone'
import { useFetch } from '../hooks/useFetch'
import { useUtente } from '../context/UtenteContext'
import { getPartita } from '../api/partite'

// Ogni quanto richiedere lo stato mentre la partita è in corso
const ATTESA_MS = 2000

// /partita/:id → il "cuore" del flusso Gioca:
//   1. partita appena avviata → "Stiamo simulando" (matchmaking)
//   2. quando è pronta        → parte da sola la battaglia animata (/partita/:id/battaglia)
//   3. finita la battaglia    → si torna qui e si vede "Hai vinto / Hai perso"
// Come capisce se è "appena avviata"? Chi avvia la partita ci arriva con
// navigate(..., { state: { nuova: true } }). Se apri il link in un altro modo
// (es. "‹ Risultato" dai dettagli) vedi subito il risultato, senza battaglia.
export default function Partita() {
  // useParams legge i pezzi variabili dell'indirizzo: /partita/42 → id = "42"
  const { id } = useParams()
  const caricaPartita = useCallback((signal) => getPartita(id, signal), [id])
  const { dati: partita, caricamento, errore, ricarica } = useFetch(caricaPartita)
  const { ricarica: ricaricaUtente } = useUtente()
  const navigate = useNavigate()
  const nuova = useLocation().state?.nuova === true

  const inCorso = partita?.stato === 'in_corso'
  const conclusa = partita?.stato === 'conclusa'

  // Finché la partita è in corso, richiedi lo stato ogni 2 secondi
  useEffect(() => {
    if (!inCorso) return
    const timer = setTimeout(ricarica, ATTESA_MS)
    return () => clearTimeout(timer)
  }, [partita, inCorso, ricarica])

  // Partita nuova e pronta → via con la battaglia animata.
  // replace = la pagina del matchmaking non resta nella cronologia del browser
  useEffect(() => {
    if (conclusa && nuova) navigate(`/partita/${id}/battaglia`, { replace: true })
  }, [conclusa, nuova, navigate, id])

  // Quando vediamo il risultato, aggiorna i crediti nella barra in alto
  useEffect(() => {
    if (conclusa && !nuova) ricaricaUtente()
  }, [conclusa, nuova, ricaricaUtente])

  // La connessione è caduta durante la battaglia (Figma E3):
  // il backend finisce la partita da solo, il risultato sarà nella cronologia.
  if (partita?.stato === 'interrotta') {
    return (
      <SchermataErrore
        titolo="Partita interrotta"
        testo="La connessione si è interrotta durante la battaglia. La partita viene simulata dal sistema: trovi il risultato nella cronologia."
        icona="⏸️"
        colore="avviso"
      >
        <BottoneLink to="/cronologia">Vai alla cronologia</BottoneLink>
        <BottoneLink to="/" variante="secondario">
          Torna alla home
        </BottoneLink>
      </SchermataErrore>
    )
  }

  // Sfondo arena su desktop solo per il risultato (come nel Figma)
  const sfondoDesktop = conclusa && !nuova ? '/images/modalita-casuale.png' : undefined

  return (
    <AppLayout paginaAttiva="gioca" sfondoMobile="/images/sfondo-gioca.png" sfondoDesktop={sfondoDesktop} nascondiMenuMobile>
      {caricamento && (
        <div className="flex flex-col items-center gap-4 lg:items-start">
          <Scheletro className="h-8 w-64" />
          <Scheletro className="h-4 w-72" />
        </div>
      )}
      {errore && <MessaggioErrore testo="Impossibile caricare la partita. Riprova più tardi." />}
      {/* Mentre aspettiamo (o un attimo prima di partire con la battaglia): matchmaking */}
      {(inCorso || (conclusa && nuova)) && <Simulazione partita={partita} />}
      {conclusa && !nuova && <Risultato partita={partita} />}
    </AppLayout>
  )
}
