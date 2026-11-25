import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Confronto from '../components/partita/Confronto'
import Bottone, { BottoneLink } from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { annullaSfida, getSfida, inviaSfida } from '../api/sfide'
import { iniziali } from '../utils/testo'

const ATTESA_MS = 2000 // ogni quanto chiedere se l'amico ha risposto
const CONTO_ALLA_ROVESCIA = 3 // secondi prima di far partire la partita

/**
 * A3 / A4 / DA3 / DA4 · /sfida/:id — aspetta la risposta dell'amico
 *
 * @returns {JSX.Element}
 */
export default function AttesaSfida() {
  const { id } = useParams()
  const navigate = useNavigate()
  const caricaSfida = useCallback((signal) => getSfida(id, signal), [id])
  const { dati: sfida, caricamento, errore, ricarica } = useFetch(caricaSfida)
  const [annullo, setAnnullo] = useState(false)

  const inAttesa = sfida?.stato === 'in_attesa'
  const accettata = sfida?.stato === 'accettata'

  // Finché è in attesa, chiedi ogni 2 secondi se l'amico ha risposto
  useEffect(() => {
    if (!inAttesa) return
    const timer = setTimeout(ricarica, ATTESA_MS)
    return () => clearTimeout(timer)
  }, [sfida, inAttesa, ricarica])

  async function annulla() {
    setAnnullo(true)
    try {
      await annullaSfida(id)
      navigate('/gioca/amico')
    } catch {
      setAnnullo(false)
    }
  }

  return (
    <AppLayout paginaAttiva="gioca" sfondoMobile="/images/sfondo-gioca.png" nascondiMenuMobile>
      {caricamento && <Scheletro className="mx-auto h-8 w-64 lg:mx-0" />}
      {errore && <MessaggioErrore testo="Impossibile caricare la sfida. Riprova più tardi." />}

      {inAttesa && (
        <div className="flex flex-1 flex-col gap-6" aria-live="polite">
          <div className="flex flex-col gap-2.5 text-center lg:gap-1.5 lg:text-left">
            <h1 className="font-titolo text-[22px] lg:text-h2">Richiesta inviata</h1>
            <p>
              Hai scelto {sfida.mioMazzo}. Aspettiamo la risposta di {sfida.amico.nome}
              <span className="lg:hidden">.</span>
              <span className="hidden lg:inline">
                : la richiesta scade tra <SecondiRimasti scadeIl={sfida.scadeIl} /> secondi.
              </span>
            </p>
            <p className="text-didascalia leading-[1.4] font-medium lg:hidden">
              La richiesta scade tra <SecondiRimasti scadeIl={sfida.scadeIl} /> secondi.
            </p>
          </div>

          <div className="mt-10 lg:mt-0">
            <Confronto inizialiAvversario={iniziali(sfida.amico.nome)} />
          </div>

          <Bottone variante="secondario" onClick={annulla} disabled={annullo} className="mt-auto min-h-12 w-full lg:mt-0 lg:w-auto lg:self-start">
            Annulla richiesta
          </Bottone>
        </div>
      )}

      {accettata && <SfidaAccettata sfida={sfida} />}

      {/* 2i · 2j · Rifiutata o scaduta */}
      {sfida && !inAttesa && !accettata && <SfidaFinita sfida={sfida} />}
    </AppLayout>
  )
}

/**
 * Mostra i secondi che mancano alla scadenza e si aggiorna da solo ogni secondo
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.scadeIl
 * @returns {JSX.Element}
 */
function SecondiRimasti({ scadeIl }) {
  const [secondi, setSecondi] = useState(() => secondiA(scadeIl))

  useEffect(() => {
    const timer = setInterval(() => setSecondi(secondiA(scadeIl)), 1000)
    return () => clearInterval(timer)
  }, [scadeIl])

  return secondi
}

/**
 * Quanti secondi mancano a una certa data (mai sotto zero)
 *
 * @param {*} dataIso
 */
function secondiA(dataIso) {
  return Math.max(0, Math.ceil((new Date(dataIso) - Date.now()) / 1000))
}

/**
 * A4 / DA4 · "@amico ha accettato!" + conto alla rovescia, poi parte la partita
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.sfida
 * @returns {JSX.Element}
 */
function SfidaAccettata({ sfida }) {
  const navigate = useNavigate()
  const [secondi, setSecondi] = useState(CONTO_ALLA_ROVESCIA)

  useEffect(() => {
    if (secondi === 0) {
      navigate(`/partita/${sfida.partitaId}`, { state: { nuova: true } })
      return
    }
    const timer = setTimeout(() => setSecondi(secondi - 1), 1000)
    return () => clearTimeout(timer)
  }, [secondi, navigate, sfida.partitaId])

  return (
    <div className="flex flex-col gap-6" aria-live="polite">
      <div className="text-center lg:flex lg:flex-col lg:gap-1.5 lg:text-left">
        <h1 className="font-titolo text-[22px] lg:text-h2">{sfida.amico.nome} ha accettato!</h1>
        <p className="hidden lg:block">
          {sfida.mioMazzo} contro {sfida.mazzoAmico}. La simulazione parte tra {secondi}…
        </p>
      </div>

      <div className="mt-28 lg:mt-0">
        <Confronto inizialiAvversario={iniziali(sfida.amico.nome)} centro="VS" conOnde={false} avversarioSbiadito={false} />
      </div>

      <div className="flex flex-col gap-7 text-center lg:hidden">
        <p className="whitespace-pre-wrap">{`${sfida.mioMazzo}  vs  ${sfida.mazzoAmico}`}</p>
        <p className="leading-[1.4] font-semibold">La simulazione parte tra {secondi}…</p>
      </div>
    </div>
  )
}

/**
 * 2i "Sfida rifiutata" · 2j "Nessuna risposta"
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.sfida
 * @returns {JSX.Element}
 */
function SfidaFinita({ sfida }) {
  const navigate = useNavigate()
  const rifiutata = sfida.stato === 'rifiutata'

  async function inviaDiNuovo() {
    const nuova = await inviaSfida(sfida.amico.id)
    navigate(`/sfida/${nuova.id}/mazzo`)
  }

  return (
    <div className="flex flex-1 flex-col gap-6" aria-live="polite">
      <div className="flex flex-col gap-2.5 text-center lg:gap-1.5 lg:text-left">
        <h1 className="font-titolo text-[22px] lg:text-h2">{rifiutata ? 'Sfida rifiutata' : 'Nessuna risposta'}</h1>
        <p>
          {rifiutata
            ? `${sfida.amico.nome} non può giocare adesso. Prova più tardi o sfida un altro amico.`
            : `${sfida.amico.nome} non ha risposto entro 60 secondi. La richiesta è scaduta.`}
        </p>
      </div>
      <span
        aria-hidden="true"
        className={`mx-auto mt-10 flex size-28 items-center justify-center rounded-full text-3xl lg:mx-0 lg:mt-0 ${
          rifiutata ? 'border-2 border-errore bg-errore-sfondo' : 'border-2 border-oro bg-sfondo'
        }`}
      >
        {rifiutata ? '✕' : '⏱'}
      </span>
      <div className="mt-auto flex flex-col gap-2.5 lg:mt-0 lg:flex-row">
        {rifiutata ? (
          <BottoneLink to="/gioca/amico" className="w-full lg:w-auto lg:min-w-60">
            Sfida un altro amico
          </BottoneLink>
        ) : (
          <Bottone onClick={inviaDiNuovo} className="w-full lg:w-auto lg:min-w-60">
            Invia di nuovo
          </Bottone>
        )}
        <BottoneLink to="/" variante="secondario" className="w-full lg:w-auto lg:min-w-60">
          Torna alla home
        </BottoneLink>
      </div>
    </div>
  )
}
