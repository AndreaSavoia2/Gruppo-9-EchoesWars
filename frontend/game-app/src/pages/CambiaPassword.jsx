import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import Bottone from '../components/Bottone'
import MessaggioErrore from '../components/MessaggioErrore'
import { cambiaPassword } from '../api/profilo'
import { cancellaToken, vaiAlLogin } from '../api/token'

// Il backend vuole almeno 12 caratteri (PasswordPolicy)
const LUNGHEZZA_MINIMA = 12

/**
 * /profilo/password · Cambia password.
 * Chiede la password attuale e quella nuova (scritta due volte per evitare errori di battitura).
 * Dopo il cambio il backend chiude tutte le sessioni: l'utente deve rifare l'accesso.
 *
 * @returns {JSX.Element}
 */
export default function CambiaPassword() {
  const [campi, setCampi] = useState({ attuale: '', nuova: '', ripeti: '' })
  const [stato, setStato] = useState({ inCorso: false, errore: null, fatto: false })

  // Controlli fatti subito nel browser, prima di disturbare il backend
  const troppoCorta = campi.nuova.length > 0 && campi.nuova.length < LUNGHEZZA_MINIMA
  const diverse = campi.ripeti.length > 0 && campi.ripeti !== campi.nuova
  const uguale = campi.nuova.length > 0 && campi.nuova === campi.attuale
  const pronta =
    campi.attuale.length > 0 && campi.nuova.length >= LUNGHEZZA_MINIMA && campi.ripeti === campi.nuova && !uguale

  /**
   * Aggiorna un campo del modulo.
   * @param {'attuale'|'nuova'|'ripeti'} nome - quale campo
   * @param {string} valore - il testo scritto
   */
  function aggiorna(nome, valore) {
    setCampi({ ...campi, [nome]: valore })
  }

  /**
   * Invia il cambio password al backend.
   * @param {Event} evento - l'invio del modulo
   */
  async function invia(evento) {
    evento.preventDefault()
    if (!pronta) return
    setStato({ inCorso: true, errore: null, fatto: false })
    try {
      await cambiaPassword(campi.attuale, campi.nuova)
      cancellaToken() // il backend ha chiuso la sessione: il token non vale più
      setStato({ inCorso: false, errore: null, fatto: true })
    } catch (errore) {
      setStato({ inCorso: false, errore: messaggioErrore(errore), fatto: false })
    }
  }

  if (stato.fatto) {
    return (
      <AppLayout sfondoMobile={null}>
        <div className="flex flex-col gap-4 lg:max-w-xl">
          <Intestazione titolo="Password cambiata" sottotitolo="Per sicurezza abbiamo chiuso tutte le sessioni aperte." />
          <Avviso tipo="successo">La nuova password è attiva. Rifai l’accesso con la nuova password.</Avviso>
          <Bottone
            onClick={() => {
              if (!vaiAlLogin('password')) window.location.href = '/'
            }}
            className="w-full lg:min-h-12 lg:w-auto lg:min-w-50 lg:self-start"
          >
            Vai all’accesso
          </Bottone>
        </div>
      </AppLayout>
    )
  }

  return (
    <AppLayout sfondoMobile={null}>
      <form onSubmit={invia} noValidate className="flex flex-col gap-4 lg:max-w-xl">
        <Intestazione titolo="Cambia password" sottotitolo="Serve la password attuale." />
        <Link to="/profilo" className="self-start font-medium underline">
          ‹ Profilo
        </Link>

        <CampoPassword
          id="password-attuale"
          etichetta="Password attuale"
          valore={campi.attuale}
          onCambia={(valore) => aggiorna('attuale', valore)}
          autocomplete="current-password"
        />
        <CampoPassword
          id="password-nuova"
          etichetta="Nuova password"
          aiuto={`Almeno ${LUNGHEZZA_MINIMA} caratteri.`}
          valore={campi.nuova}
          onCambia={(valore) => aggiorna('nuova', valore)}
          autocomplete="new-password"
          errore={
            troppoCorta
              ? `La password deve avere almeno ${LUNGHEZZA_MINIMA} caratteri.`
              : uguale
                ? 'La nuova password deve essere diversa da quella attuale.'
                : null
          }
        />
        <CampoPassword
          id="password-ripeti"
          etichetta="Ripeti la nuova password"
          valore={campi.ripeti}
          onCambia={(valore) => aggiorna('ripeti', valore)}
          autocomplete="new-password"
          errore={diverse ? 'Le due password non sono uguali.' : null}
        />

        <Avviso tipo="info" icona="🔒">
          Dopo il cambio dovrai rifare l’accesso su tutti i dispositivi.
        </Avviso>

        <Bottone type="submit" disabled={!pronta || stato.inCorso} className="w-full lg:min-h-12 lg:w-auto lg:min-w-50 lg:self-start">
          {stato.inCorso ? 'Salvataggio…' : 'Cambia password'}
        </Bottone>
        {stato.errore && <MessaggioErrore testo={stato.errore} />}
      </form>
    </AppLayout>
  )
}

/**
 * Un campo password con etichetta, testo di aiuto e messaggio di errore.
 * Il bottone "Mostra" fa vedere quello che si sta scrivendo.
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.id - id del campo (collega etichetta e messaggi)
 * @param {string} props.etichetta - testo sopra il campo
 * @param {string} props.valore - testo scritto
 * @param {Function} props.onCambia - chiamata con il nuovo testo
 * @param {string} props.autocomplete - aiuta il browser a suggerire/salvare la password
 * @param {string} [props.aiuto] - suggerimento sotto il campo
 * @param {string|null} [props.errore] - messaggio di errore (rosso)
 * @returns {JSX.Element}
 */
function CampoPassword({ id, etichetta, valore, onCambia, autocomplete, aiuto, errore }) {
  const [visibile, setVisibile] = useState(false)
  // aria-describedby: lo screen reader legge anche aiuto ed errore
  const descrizione = [aiuto && `${id}-aiuto`, errore && `${id}-errore`].filter(Boolean).join(' ') || undefined

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="font-semibold">
        {etichetta}
      </label>
      <div className="flex gap-2">
        <input
          id={id}
          type={visibile ? 'text' : 'password'}
          value={valore}
          onChange={(evento) => onCambia(evento.target.value)}
          autoComplete={autocomplete}
          aria-invalid={Boolean(errore)}
          aria-describedby={descrizione}
          className={`min-h-12 min-w-0 flex-1 rounded-[14px] border-[1.5px] bg-sfondo px-3.5 ${
            errore ? 'border-errore' : 'border-testo'
          }`}
        />
        <button
          type="button"
          onClick={() => setVisibile(!visibile)}
          aria-pressed={visibile}
          aria-label={visibile ? `Nascondi ${etichetta.toLowerCase()}` : `Mostra ${etichetta.toLowerCase()}`}
          className="min-h-12 shrink-0 rounded-[14px] border-[1.5px] border-testo px-3.5 font-medium hover:bg-sfondo-secondario"
        >
          {visibile ? 'Nascondi' : 'Mostra'}
        </button>
      </div>
      {aiuto && (
        <p id={`${id}-aiuto`} className="text-didascalia text-testo-secondario">
          {aiuto}
        </p>
      )}
      {errore && (
        <p id={`${id}-errore`} className="text-didascalia font-medium text-errore">
          {errore}
        </p>
      )}
    </div>
  )
}

/**
 * Trasforma l'errore del backend in un messaggio comprensibile.
 * @param {Error & { stato?: number, messaggio?: string }} errore
 * @returns {string}
 */
function messaggioErrore(errore) {
  if (errore.stato === 401) return 'La password attuale non è corretta.'
  if (errore.stato === 400 && /differ/i.test(errore.messaggio ?? '')) return 'La nuova password deve essere diversa da quella attuale.'
  if (errore.stato === 400) return `La nuova password non è valida: servono almeno ${LUNGHEZZA_MINIMA} caratteri.`
  return 'Cambio password non riuscito. Riprova più tardi.'
}
