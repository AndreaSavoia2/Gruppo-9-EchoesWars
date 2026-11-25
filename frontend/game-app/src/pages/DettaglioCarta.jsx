import { useCallback, useState } from 'react'
import { Link, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Bottone, { BottoneLink } from '../components/Bottone'
import Modale from '../components/Modale'
import CartaIngrandita from '../components/collezione/CartaIngrandita'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { useUtente } from '../context/UtenteContext'
import { convertiCopie, getCarta } from '../api/collezione'
import { bordoRarita } from '../data/rarita'
import { normalizzaCarta } from '../data/carte'
import ImmagineCarta from '../components/carta/ImmagineCarta'

// Colore dell'etichetta per ogni Eco (colori "fazioni" del Figma)
const coloreEco = {
  Dominio: 'border-dominio text-dominio',
  Sapienza: 'border-sapienza text-sapienza',
  Inganno: 'border-inganno text-inganno',
  Oblio: 'border-oblio text-oblio',
}

/**
 * 6 · 6c · 6d · 6e / D6 · D6b · D6c · /collezione/carta/:id
 *
 * @returns {JSX.Element}
 */
export default function DettaglioCarta() {
  const { id } = useParams()
  const caricaCarta = useCallback((signal) => getCarta(id, signal), [id])
  const { dati, caricamento, errore, ricarica } = useFetch(caricaCarta)
  // Traduce i codici del backend in italiano (es. "DOMINION" → "Dominio", "COMMON" → "Comune")
  const carta = dati && normalizzaCarta(dati)
  const { ricarica: ricaricaUtente } = useUtente()

  const [confermaAperta, setConfermaAperta] = useState(false)
  const [conversione, setConversione] = useState({ inCorso: false, esito: null, errore: null })
  const chiudiConferma = useCallback(() => setConfermaAperta(false), [])
  // 6e: carta ingrandita aperta sì/no
  const [ingrandita, setIngrandita] = useState(false)
  const chiudiIngrandita = useCallback(() => setIngrandita(false), [])

  async function converti() {
    setConversione({ inCorso: true, esito: null, errore: null })
    try {
      const esito = await convertiCopie(id)
      setConversione({ inCorso: false, esito, errore: null })
      setConfermaAperta(false)
      ricarica() // aggiorna le copie della carta
      ricaricaUtente() // aggiorna i crediti in alto
    } catch {
      setConversione({ inCorso: false, esito: null, errore: 'Conversione non riuscita. Riprova.' })
    }
  }

  return (
    <AppLayout paginaAttiva="collezione" sfondoMobile="/images/sfondo-collezione.png" sfondoDesktop="/images/sfondo-collezione.png">
      <div className="-mt-6 flex flex-col gap-3 lg:mt-0 lg:gap-6">
        <Link to="/collezione" className="flex min-h-11 items-center self-start font-medium lg:min-h-0 lg:underline">
          ‹ Collezione
        </Link>

        {caricamento && <Scheletro className="mx-auto h-94.75 w-67.5 rounded-lg lg:mx-0 lg:h-133.25 lg:w-95" />}
        {errore && <MessaggioErrore testo="Impossibile caricare la carta. Riprova più tardi." />}

        {carta && (
          <div className="flex flex-col items-center gap-3 lg:flex-row lg:items-start lg:gap-10">
            {/* Carta grande con le copie: è un bottone, toccandola si apre la versione ingrandita (6e) */}
            <div className="flex shrink-0 flex-col items-center gap-3">
              <button
                type="button"
                onClick={() => setIngrandita(true)}
                aria-label={`Ingrandisci la carta ${carta.nome}`}
                className="relative h-94.75 w-67.5 cursor-zoom-in lg:h-133.25 lg:w-95"
              >
                <ImmagineCarta carta={carta} className="size-full lg:rounded-3xl" />
                <span className="absolute -top-1.5 right-1 rounded-full border-[1.5px] border-sfondo bg-testo px-2 py-0.5 text-didascalia leading-[1.4] font-medium text-sfondo">
                  ×{carta.quantita}
                  <span className="sr-only"> copie</span>
                </span>
              </button>
              {/* aria-hidden: per lo screen reader basta l'etichetta del bottone */}
              <p aria-hidden="true" className="text-didascalia leading-[1.4] font-medium text-testo-secondario">
                🔍 <span className="lg:hidden">Tocca</span>
                <span className="hidden lg:inline">Clicca</span> la carta per vederla in grande
              </p>
            </div>

            <div className="flex w-full flex-col items-center gap-3 lg:flex-1 lg:items-start lg:gap-5">
              <h1 className="text-center font-titolo text-[22px] lg:text-h2">{carta.nome}</h1>

              {/* Etichette: colorate su mobile, neutre con "Eco:" e "Scuola:" su desktop */}
              <ul className="flex flex-wrap justify-center gap-2">
                <Etichetta mobile={carta.eco} desktop={`Eco: ${carta.eco}`} colore={coloreEco[carta.eco]} />
                <Etichetta mobile={carta.scuola} desktop={`Scuola: ${carta.scuola}`} colore="border-testo text-testo" />
                <Etichetta mobile={carta.rarita} desktop={`◆ ${carta.rarita}`} colore={bordoRarita[carta.rarita]} />
              </ul>

              <Valori carta={carta} />

              {/* 💻 Desktop: abilità e copie */}
              <section className="hidden w-full flex-col gap-1.5 rounded-2xl border border-oro bg-sfondo p-6 lg:flex">
                <h2 className="leading-[1.4] font-semibold">Abilità: {carta.effetto?.nome ?? carta.abilita ?? "Nessuna"}</h2>
                <p>
                  Ne possiedi {carta.quantita} copie · {carta.copieNeiMazzi} sono nei tuoi mazzi attivi.
                </p>
              </section>

              {/* 6d · Conversione riuscita */}
              {conversione.esito && (
                <p role="status" className="flex w-full gap-2.5 rounded-xl border border-successo bg-successo-sfondo px-3.5 py-3 text-didascalia leading-[1.4] font-medium">
                  <span aria-hidden="true" className="text-successo">✓</span>
                  +{conversione.esito.creditiAggiunti} crediti aggiunti. Ti restano {conversione.esito.quantita} copie di
                  questa carta.
                </p>
              )}

              <div className="flex w-full flex-col gap-2.5 lg:flex-row lg:gap-3">
                <BottoneLink to="/collezione/mazzi" className="w-full lg:min-h-12 lg:w-auto lg:min-w-50">
                  Aggiungi a un mazzo
                </BottoneLink>
                <BottoneLink to="/scambi" variante="secondario" className="w-full lg:min-h-12 lg:w-auto lg:min-w-50">
                  Proponi uno scambio
                </BottoneLink>
                {/* 📱 Mobile: bottone rosso per convertire */}
                {carta.conversione && (
                  <Bottone variante="distruttivo" onClick={() => setConfermaAperta(true)} className="w-full lg:hidden">
                    Converti {carta.conversione.copie} copie in crediti
                  </Bottone>
                )}
              </div>

              {/* 💻 Desktop: riquadro "Converti i doppioni" */}
              {carta.conversione && (
                <section className="hidden w-full flex-col items-start gap-2.5 rounded-2xl border border-oro bg-sfondo p-6 lg:flex">
                  <h2 className="font-titolo text-h2">Converti i doppioni in crediti</h2>
                  <p>
                    Puoi convertire {carta.conversione.copie} copie identiche di questa carta. Per una carta{' '}
                    {carta.rarita} ricevi {carta.conversione.crediti} crediti.
                  </p>
                  <Avviso />
                  <Bottone variante="distruttivo" onClick={() => setConfermaAperta(true)} className="min-h-12 w-85">
                    Converti {carta.conversione.copie} copie in {carta.conversione.crediti} crediti
                  </Bottone>
                </section>
              )}
            </div>
          </div>
        )}
      </div>

      {/* 6c / D6b · Conferma prima di eliminare le copie */}
      {ingrandita && carta && <CartaIngrandita carta={carta} onChiudi={chiudiIngrandita} />}
      {confermaAperta && carta?.conversione && (
        <Modale titolo={`Convertire ${carta.conversione.copie} copie?`} onChiudi={chiudiConferma}>
          <p className="text-testo-secondario">
            Riceverai {carta.conversione.crediti} crediti (carta {carta.rarita}). Le {carta.conversione.copie} copie di
            “{carta.nome}” verranno eliminate per sempre. Ti resteranno {carta.quantita - carta.conversione.copie} copie.
          </p>
          <Bottone variante="distruttivo" onClick={converti} disabled={conversione.inCorso}>
            {conversione.inCorso ? 'Conversione…' : `Converti e ricevi ${carta.conversione.crediti} crediti`}
          </Bottone>
          <Bottone variante="secondario" onClick={chiudiConferma}>
            Annulla
          </Bottone>
          {conversione.errore && <MessaggioErrore testo={conversione.errore} />}
        </Modale>
      )}
    </AppLayout>
  )
}

/**
 * Etichetta della carta (Eco, Scuola, Rarità): testo diverso su mobile e desktop.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.mobile
 * @param {*} props.desktop
 * @param {string} props.colore
 * @returns {JSX.Element}
 */
function Etichetta({ mobile, desktop, colore }) {
  return (
    <li>
      <span className={`block rounded-full border-[1.5px] bg-sfondo px-3 py-1 text-didascalia leading-[1.4] font-medium lg:hidden ${colore}`}>
        {mobile}
      </span>
      <span className="hidden rounded-full border border-oro bg-sfondo-secondario px-3.5 py-2 leading-[1.4] font-semibold lg:block">
        {desktop}
      </span>
    </li>
  )
}

/**
 * ATK · DEF · Costo (+ Effetto solo su mobile)
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.carta
 * @returns {JSX.Element}
 */
function Valori({ carta }) {
  const valori = [
    ['ATK', carta.atk],
    ['DEF', carta.def],
    ['Costo', carta.costo],
  ]
  return (
    <dl className="flex w-full gap-2.5 lg:gap-3">
      {valori.map(([nome, valore]) => (
        <div
          key={nome}
          className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-xl bg-sfondo-secondario py-2 leading-[1.4] lg:rounded-2xl lg:border lg:border-oro lg:px-4.5 lg:py-3.5"
        >
          <dt className="text-didascalia font-medium text-testo-secondario lg:text-testo">{nome}</dt>
          <dd className="font-semibold lg:font-titolo lg:text-[36px] lg:leading-normal lg:font-normal">{valore}</dd>
        </div>
      ))}
      <div className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-xl bg-sfondo-secondario py-2 leading-[1.4] lg:hidden">
        <dt className="text-didascalia font-medium text-testo-secondario">Effetto</dt>
        <dd className="font-semibold">{carta.effetto?.nome ?? carta.abilita ?? "Nessuna"}</dd>
      </div>
    </dl>
  )
}

/**
 * Avviso rosso: le copie convertite vengono eliminate per sempre.
 *
 * @returns {JSX.Element}
 */
function Avviso() {
  return (
    <p className="flex w-full gap-2.5 rounded-xl border border-errore bg-errore-sfondo px-3.5 py-3 text-didascalia leading-[1.4] font-medium">
      <span aria-hidden="true" className="text-corpo font-semibold text-errore">⚠</span>
      Le copie convertite vengono eliminate per sempre. Le carte usate nei mazzi attivi non vengono convertite.
    </p>
  )
}
