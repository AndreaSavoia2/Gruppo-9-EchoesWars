import { useCallback } from 'react'
import { Link, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import { BottoneLink } from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { getPartita } from '../api/partite'
import { dataLeggibile, iniziali } from '../utils/testo'

const esiti = {
  vittoria: { testo: 'Vittoria', icona: '/icons/vittoria.svg', emoji: '🏆', stile: 'border-successo bg-successo-sfondo text-successo' },
  pareggio: { testo: 'Pareggio', icona: '/icons/pareggio.svg', emoji: '🤝', stile: 'border-neutro bg-sfondo-secondario text-neutro' },
  sconfitta: { testo: 'Sconfitta', icona: '/icons/sconfitta.svg', emoji: '✕', stile: 'border-errore bg-errore-sfondo text-errore' },
}

/**
 * G6 / DG5 · /partita/:id/dettagli
 *
 * @returns {JSX.Element}
 */
export default function DettagliPartita() {
  const { id } = useParams()
  const caricaPartita = useCallback((signal) => getPartita(id, signal), [id])
  const { dati: partita, caricamento, errore } = useFetch(caricaPartita)

  return (
    <AppLayout paginaAttiva="gioca" sfondoMobile="/images/sfondo-gioca.png">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <Intestazione titolo="Dettaglio partita" sottotitolo={partita ? dataLeggibile(partita.giocataIl) : ''}>
          <Link to={`/partita/${id}`} className="hidden font-medium underline lg:block">
            ‹ Risultato
          </Link>
        </Intestazione>

        {/* 📱 Mobile: "‹ Risultato" sotto il titolo, color oro scuro */}
        <Link to={`/partita/${id}`} className="mt-2 flex min-h-11 items-center self-start font-medium text-testo-secondario lg:hidden">
          ‹ Risultato
        </Link>

        {caricamento && <Scheletro className="h-96 rounded-2xl" />}
        {errore && <MessaggioErrore testo="Impossibile caricare la partita. Riprova più tardi." />}
        {partita && <Contenuto partita={partita} />}
      </div>
    </AppLayout>
  )
}

/**
 * Contenuto del dettaglio partita: esito, giocatori, statistiche e bottoni.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.partita
 * @returns {JSX.Element}
 */
function Contenuto({ partita }) {
  const esito = esiti[partita.esito]
  const { avversario } = partita

  // Righe della tabella: etichetta + valore
  const statistiche = [
    ['Round giocati', `${partita.round} / ${partita.roundMax}`],
    ['PV finali', `${partita.pvMiei} – ${partita.pvAvversario}`],
    ['Carte rimaste', `${partita.carteRimasteMie} – ${partita.carteRimasteAvversario}`],
    ['Come è finita', partita.motivoFine],
    ['Crediti guadagnati', `+${partita.creditiGuadagnati}`],
  ]

  return (
    <>
      <div className="flex flex-col gap-2.5 lg:flex-row lg:items-start lg:gap-6">
        {/* Esito + i due giocatori */}
        <section className="flex flex-col items-center gap-2.5 rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5 lg:w-105 lg:gap-3 lg:border-oro lg:p-6">
          <p className={`flex items-center gap-1.5 rounded-full border-[1.5px] px-3.5 py-1.5 leading-[1.4] font-semibold lg:border ${esito.stile}`}>
            <img src={esito.icona} alt="" className="size-4.5 lg:hidden" />
            <span aria-hidden="true" className="hidden lg:inline">{esito.emoji}</span>
            {esito.testo}
          </p>
          <div className="flex items-start gap-2 lg:items-center lg:gap-6">
            <Giocatore iniziali="TU" nome="Tu" mazzo={partita.mioMazzo} />
            <span className="mt-3 font-titolo text-[28px] text-testo-secondario lg:mt-0 lg:text-[36px] lg:text-testo">VS</span>
            <Giocatore iniziali={iniziali(avversario.nome)} nome={avversario.nome} mazzo={avversario.mazzo} />
          </div>
        </section>

        {/* Statistiche: <dl> = lista di "nome → valore" */}
        <section className="flex flex-col rounded-2xl border border-oro-bordo bg-sfondo px-4 pt-3 pb-1.5 lg:flex-1 lg:gap-2.5 lg:border-oro lg:p-6">
          <p className="text-didascalia lg:leading-[1.4] lg:font-medium">Valori: Tu – {avversario.nome}</p>
          <dl>
            {statistiche.map(([etichetta, valore]) => (
              <div
                key={etichetta}
                className="flex items-center justify-between border-b-[0.5px] border-oro-bordo py-2 last:border-b-0 lg:border-b-0 lg:py-1"
              >
                <dt>{etichetta}</dt>
                <dd className="leading-[1.4] font-semibold">{valore}</dd>
              </div>
            ))}
          </dl>
        </section>
      </div>

      <div className="flex flex-col gap-2.5 lg:flex-row lg:gap-4">
        <BottoneLink to={`/partita/${partita.id}/replay`} className="w-full lg:min-h-12 lg:w-50">
          ▶ Rivedi partita
        </BottoneLink>
        <BottoneLink to="/" variante="secondario" className="w-full lg:min-h-12 lg:w-50">
          Torna alla home
        </BottoneLink>
      </div>
    </>
  )
}

/**
 * Un giocatore nel riquadro "VS": iniziali, nome e mazzo usato.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.iniziali
 * @param {string} props.nome
 * @param {*} props.mazzo
 * @returns {JSX.Element}
 */
function Giocatore({ iniziali, nome, mazzo }) {
  return (
    <div className="flex w-35 flex-col items-center gap-1 text-center leading-[1.4] lg:w-auto">
      <span className="flex size-16 items-center justify-center rounded-full border-2 border-oro-bordo bg-oro font-titolo text-2xl lg:size-18 lg:border lg:border-oro lg:bg-sfondo-secondario lg:text-h2">
        {iniziali}
      </span>
      <span className="font-semibold">{nome}</span>
      <span className="text-didascalia font-medium">{mazzo}</span>
    </div>
  )
}
