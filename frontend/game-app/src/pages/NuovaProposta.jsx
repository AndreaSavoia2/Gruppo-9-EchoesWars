import { useCallback, useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import Bottone, { BottoneLink } from '../components/Bottone'
import RigaSelezionabile from '../components/RigaSelezionabile'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import CoppiaCarte from '../components/scambi/CoppiaCarte'
import CartaSelezionabile from '../components/scambi/CartaSelezionabile'
import { useFetch } from '../hooks/useFetch'
import { getAmiciScambio, getCarteAmico, getMieCarteScambio, inviaProposta } from '../api/scambi'
import { REGOLE_SCAMBI } from '../data/regole'
import { dataBreve } from '../utils/testo'

/**
 * 10b-10f · /scambi/nuova — una sola pagina, 5 momenti:
 *   1 amico → 2 la tua carta → 3 la carta che vuoi → 4 riepilogo → 5 inviata
 *
 * @returns {JSX.Element}
 */
export default function NuovaProposta() {
  const [passo, setPasso] = useState(1)
  const [amico, setAmico] = useState(null)
  const [miaCarta, setMiaCarta] = useState(null)
  const [suaCarta, setSuaCarta] = useState(null)
  const [invio, setInvio] = useState({ inCorso: false, errore: null })

  async function invia() {
    setInvio({ inCorso: true, errore: null })
    try {
      await inviaProposta({ amicoId: amico.id, cartaOffertaId: miaCarta.id, cartaRichiestaId: suaCarta.id })
      setPasso(5)
    } catch {
      setInvio({ inCorso: false, errore: 'Invio non riuscito. Riprova.' })
    }
  }

  // Testi di ogni passo: titolo, sottotitolo, "‹ indietro"
  const testi = {
    1: ['Nuova proposta', 'Una carta contro una della stessa rarità.', 'Scambi'],
    2: ['Nuova proposta', `Con ${amico?.nome}`, 'Amico'],
    3: ['Nuova proposta', `Dalla collezione di ${amico?.nome}`, 'La tua carta'],
    4: ['Riepilogo', 'Controlla prima di inviare.', 'Carta richiesta'],
    5: ['Proposta inviata', null, null],
  }
  const [titolo, sottotitolo, indietro] = testi[passo]

  return (
    <AppLayout paginaAttiva="scambi">
      <div className="flex flex-col gap-2.5 lg:max-w-3xl lg:gap-4">
        <Intestazione titolo={titolo} sottotitolo={sottotitolo} />

        {indietro &&
          (passo === 1 ? (
            <Link to="/scambi" className="mt-4 flex min-h-11 items-center self-start font-medium text-testo-secondario lg:mt-0">
              ‹ {indietro}
            </Link>
          ) : (
            <button type="button" onClick={() => setPasso(passo - 1)} className="mt-4 flex min-h-11 items-center self-start font-medium text-testo-secondario lg:mt-0">
              ‹ {indietro}
            </button>
          ))}

        {passo <= 3 && <Progresso passo={passo} />}

        {passo === 1 && (
          <PassoAmico
            scelto={amico}
            onScegli={(nuovo) => {
              // Cambi amico → le carte scelte non valgono più
              if (nuovo.id !== amico?.id) setSuaCarta(null)
              setAmico(nuovo)
            }}
            onAvanti={() => setPasso(2)}
          />
        )}
        {passo === 2 && (
          <PassoMiaCarta
            scelta={miaCarta}
            onScegli={(carta) => {
              if (carta.rarita !== miaCarta?.rarita) setSuaCarta(null)
              setMiaCarta(carta)
            }}
            onAvanti={() => setPasso(3)}
          />
        )}
        {passo === 3 && <PassoSuaCarta amico={amico} rarita={miaCarta.rarita} scelta={suaCarta} onScegli={setSuaCarta} onAvanti={() => setPasso(4)} />}

        {passo === 4 && (
          <>
            <div className="mt-2 rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5">
              <CoppiaCarte
                sinistra={{ etichetta: 'Offri', carta: miaCarta }}
                destra={{ etichetta: `Chiedi a ${amico.nome}`, carta: suaCarta }}
              />
            </div>
            <ul className="flex flex-col gap-2 rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5 text-didascalia leading-[1.4]">
              <li>
                • La proposta vale {REGOLE_SCAMBI.oreProposta} ore: se {amico.nome} non risponde, scade da sola.
              </li>
              <li>• {miaCarta.nome} resta riservata finché la proposta non viene accettata, rifiutata, annullata o scade.</li>
              <li>• Lo scambio avviene solo se {amico.nome} accetta.</li>
            </ul>
            <Bottone onClick={invia} disabled={invio.inCorso} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
              {invio.inCorso ? 'Invio…' : 'Invia proposta'}
            </Bottone>
            {invio.errore && <MessaggioErrore testo={invio.errore} />}
          </>
        )}

        {passo === 5 && (
          <>
            <div className="mt-6 flex flex-col gap-3">
              <Avviso tipo="successo" barra>
                Proposta inviata a {amico.nome}. Ti avvisiamo appena risponde.
              </Avviso>
              <div className="rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5">
                <CoppiaCarte sinistra={{ etichetta: 'Offri', carta: miaCarta }} destra={{ etichetta: 'Chiedi', carta: suaCarta }} />
              </div>
              <p className="text-center leading-[1.4] font-semibold">
                Scade tra {REGOLE_SCAMBI.oreProposta} ore se {amico.nome} non risponde.
              </p>
            </div>
            <div className="flex flex-col gap-2.5 lg:flex-row">
              <BottoneLink to="/scambi?scheda=inviate" className="w-full lg:w-auto lg:min-w-60">
                Vedi proposte inviate
              </BottoneLink>
              <BottoneLink to="/scambi" variante="secondario" className="w-full lg:w-auto lg:min-w-60">
                Torna agli scambi
              </BottoneLink>
            </div>
          </>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * "Passo 2 di 3 · ..." + barra a 3 pezzi
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.passo
 * @returns {JSX.Element}
 */
function Progresso({ passo }) {
  const nomi = ['Scegli l’amico', 'Scegli la carta da offrire', 'Scegli la carta che vuoi']
  return (
    <div className="flex flex-col gap-1.5">
      <p className="leading-[1.4] font-semibold">
        Passo {passo} di 3 · {nomi[passo - 1]}
      </p>
      <div className="flex gap-1.5" aria-hidden="true">
        {[1, 2, 3].map((n) => (
          <span key={n} className={`h-1.5 flex-1 rounded-sm ${n <= passo ? 'bg-oro-bordo' : 'bg-sfondo-secondario'}`} />
        ))}
      </div>
    </div>
  )
}

/**
 * Passo 1 della proposta di scambio: scegli l'amico.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.scelto
 * @param {Function} props.onScegli
 * @param {Function} props.onAvanti
 * @returns {JSX.Element}
 */
function PassoAmico({ scelto, onScegli, onAvanti }) {
  const { dati: amici, caricamento, errore } = useFetch(getAmiciScambio)
  return (
    <>
      {caricamento && <Scheletro className="h-72 rounded-2xl" />}
      {errore && <MessaggioErrore />}
      {amici?.length === 0 && <p>Non hai ancora amici con cui scambiare. Aggiungili dalla sezione Amici.</p>}
      <ul className="mt-2 flex flex-col gap-2.5 lg:grid lg:grid-cols-2">
        {amici?.map((amico) => (
          <li key={amico.id}>
            <RigaSelezionabile
              titolo={amico.nome}
              dettaglio={`Amico dal ${dataBreve(amico.amiciDal)}`}
              scelto={amico.id === scelto?.id}
              onScegli={() => onScegli(amico)}
            />
          </li>
        ))}
      </ul>
      <Bottone onClick={onAvanti} disabled={!scelto} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
        Avanti
      </Bottone>
    </>
  )
}

/**
 * Griglia di carte selezionabili (si può sceglierne una sola).
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.carte
 * @param {*} props.scelta
 * @param {Function} props.onScegli
 * @returns {JSX.Element}
 */
function GrigliaCarte({ carte, scelta, onScegli }) {
  return (
    <ul className="mt-4 grid grid-cols-3 gap-x-3 gap-y-2.5 lg:grid-cols-5">
      {carte.map((carta) => (
        <li key={carta.id}>
          <CartaSelezionabile carta={carta} scelta={carta.id === scelta?.id} onScegli={() => onScegli(carta)} />
        </li>
      ))}
    </ul>
  )
}

/**
 * Passo 2 della proposta di scambio: scegli la tua carta da offrire.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.scelta
 * @param {Function} props.onScegli
 * @param {Function} props.onAvanti
 * @returns {JSX.Element}
 */
function PassoMiaCarta({ scelta, onScegli, onAvanti }) {
  const { dati: carte, caricamento, errore } = useFetch(getMieCarteScambio)
  return (
    <>
      {caricamento && <Scheletro className="mt-4 h-72 rounded-2xl" />}
      {errore && <MessaggioErrore />}
      {carte && <GrigliaCarte carte={carte} scelta={scelta} onScegli={onScegli} />}
      <Bottone onClick={onAvanti} disabled={!scelta} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
        Avanti
      </Bottone>
    </>
  )
}

/**
 * Passo 3 della proposta di scambio: scegli la carta che chiedi all'amico (stessa rarità).
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.amico
 * @param {*} props.rarita
 * @param {*} props.scelta
 * @param {Function} props.onScegli
 * @param {Function} props.onAvanti
 * @returns {JSX.Element}
 */
function PassoSuaCarta({ amico, rarita, scelta, onScegli, onAvanti }) {
  const carica = useCallback((signal) => getCarteAmico(amico.id, rarita, signal), [amico.id, rarita])
  const { dati: carte, caricamento, errore } = useFetch(carica)
  return (
    <>
      {caricamento && <Scheletro className="mt-4 h-72 rounded-2xl" />}
      {errore && <MessaggioErrore />}
      {carte?.length === 0 && (
        <p className="mt-4">
          {amico.nome} non ha carte {rarita.toLowerCase()} da scambiare. Torna indietro e scegli un’altra carta.
        </p>
      )}
      {carte?.length > 0 && <GrigliaCarte carte={carte} scelta={scelta} onScegli={onScegli} />}
      <Bottone onClick={onAvanti} disabled={!scelta} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
        Rivedi la proposta
      </Bottone>
    </>
  )
}
