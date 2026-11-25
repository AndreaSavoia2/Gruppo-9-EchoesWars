import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Chip from '../components/Chip'
import BottoneGioca from '../components/BottoneGioca'
import ProgressoLeggendaria from '../components/ProgressoLeggendaria'
import Riquadro from '../components/Riquadro'
import RigaPartita from '../components/RigaPartita'
import RigaMissione from '../components/RigaMissione'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useUtente } from '../context/UtenteContext'
import { useFetch } from '../hooks/useFetch'
import { getDashboard } from '../api/dashboard'

/**
 * Home dell'app: saluto, scorciatoie, GIOCA, ultime partite e Prove degli Echi.
 *
 * @returns {JSX.Element}
 */
export default function Dashboard() {
  const { dati: utente, caricamento: caricamentoUtente } = useUtente()
  const { dati, caricamento, errore } = useFetch(getDashboard)

  return (
    <AppLayout paginaAttiva="gioca">
      <div className="flex flex-1 flex-col gap-5 lg:gap-6">
        {/* Intestazione: il nome arriva dal backend */}
        <div className="flex flex-col gap-1 lg:gap-1.5">
          {caricamentoUtente ? (
            <Scheletro className="h-8 w-64" />
          ) : (
            <h1 className="font-titolo text-h2">Ciao{utente ? `, ${utente.nome}` : ''}</h1>
          )}
          <p>La tua prossima sfida ti aspetta.</p>
        </div>

        {/* Scorciatoie: il pallino su "Richieste" appare solo se ci sono richieste nuove */}
        <div className="flex flex-wrap justify-center gap-2.5 lg:justify-start lg:gap-3">
          <Chip testo="Richieste" percorso="/richieste" notifica={dati?.richiesteNuove > 0} />
          <Chip testo="Missioni" percorso="/missioni" />
          <Chip testo="Cronologia" percorso="/cronologia" />
        </div>

        {/* Griglia: su mobile solo GIOCA, su desktop GIOCA + colonna destra */}
        <div className="flex flex-1 items-start gap-6 lg:items-stretch">
          <BottoneGioca className="flex-1" />

          <aside className="hidden w-[440px] shrink-0 flex-col gap-4 lg:flex">
            {caricamento && <ColonnaInCaricamento />}
            {errore && <MessaggioErrore />}
            {dati && <ColonnaDati dati={dati} />}
          </aside>
        </div>
      </div>
    </AppLayout>
  )
}

/**
 * Colonna destra quando i dati sono arrivati
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.dati
 * @returns {JSX.Element}
 */
function ColonnaDati({ dati }) {
  const { leggendaria, ultimePartite, prove } = dati

  return (
    <>
      <ProgressoLeggendaria buste={leggendaria.buste} totale={leggendaria.totale} />

      <Riquadro titolo="Ultime partite">
        {ultimePartite.length === 0 ? (
          <p>Non hai ancora giocato partite.</p>
        ) : (
          ultimePartite.map((partita) => (
            <RigaPartita
              key={partita.id}
              esito={partita.esito}
              avversario={partita.avversario}
              round={partita.round}
            />
          ))
        )}
        <Link to="/cronologia" className="font-medium underline">
          Vedi tutta la cronologia
        </Link>
      </Riquadro>

      <Riquadro titolo={`Prove degli Echi · ${prove.completate}/${prove.totali}`}>
        {prove.inEvidenza.map((prova) => (
          <RigaMissione
            key={prova.id}
            titolo={prova.titolo}
            ricompensa={prova.ricompensa}
            fatto={prova.fatto}
            totale={prova.totale}
          />
        ))}
        <Link to="/missioni" className="font-medium underline">
          Vedi tutte le Prove
        </Link>
      </Riquadro>
    </>
  )
}

/**
 * Colonna destra mentre aspettiamo il backend: rettangoli che pulsano
 *
 * @returns {JSX.Element}
 */
function ColonnaInCaricamento() {
  return (
    <>
      <span className="sr-only">Caricamento in corso…</span>
      <Scheletro className="h-24 rounded-2xl" />
      <Scheletro className="h-72 rounded-2xl" />
      <Scheletro className="h-52 rounded-2xl" />
    </>
  )
}
