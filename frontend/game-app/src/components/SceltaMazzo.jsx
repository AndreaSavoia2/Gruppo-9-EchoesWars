import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from './AppLayout'
import Intestazione from './Intestazione'
import MazzoPerPartita from './MazzoPerPartita'
import Bottone from './Bottone'
import Riquadro from './Riquadro'
import Scheletro from './Scheletro'
import MessaggioErrore from './MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { getMazzi } from '../api/mazzi'

// Schermata "Scegli il mazzo": la usano sia la partita casuale che la sfida a un amico.
// sottotitolo:   testo sotto il titolo (solo desktop)
// indietro:      { percorso, testo, testoMobile } per il link "‹ ..." in alto
// avvisoMobile:  testo del riquadro viola sotto il titolo (solo mobile, facoltativo)
// testoBottone:  es. "Inizia la partita"
// onConferma:    funzione chiamata col mazzo scelto quando premi il bottone
export default function SceltaMazzo({ sottotitolo, indietro, avvisoMobile, testoBottone, onConferma }) {
  const { dati, caricamento, errore } = useFetch(getMazzi)
  const mazzi = dati?.mazzi
  const [idScelto, setIdScelto] = useState(null)
  const [invio, setInvio] = useState({ inCorso: false, errore: null })

  const mazzoScelto = mazzi?.find((m) => m.id === idScelto)

  async function conferma() {
    setInvio({ inCorso: true, errore: null })
    try {
      await onConferma(mazzoScelto)
    } catch {
      setInvio({ inCorso: false, errore: 'Qualcosa è andato storto. Riprova.' })
    }
  }

  const legenda = (
    <p className="text-didascalia leading-[1.4] font-medium">🔒 = mazzo non valido: sistemalo in Collezione › Mazzi.</p>
  )

  return (
    <AppLayout paginaAttiva="gioca" sfondoMobile="/images/sfondo-mazzo.png" sfondoDesktop="/images/sfondo-mazzo-desktop.png" nascondiMenuMobile>
      <div className="flex flex-1 flex-col gap-3 lg:gap-6">
        {/* 📱 Mobile: "‹ Indietro" sopra il titolo */}
        <Link to={indietro.percorso} className="-mt-6 self-start font-medium underline lg:hidden">
          ‹ {indietro.testoMobile ?? 'Indietro'}
        </Link>

        <Intestazione titolo="Scegli il mazzo">
          <p className="hidden lg:block">{sottotitolo}</p>
          <Link to={indietro.percorso} className="hidden font-medium underline lg:block">
            ‹ {indietro.testo}
          </Link>
        </Intestazione>

        {avvisoMobile && (
          <p className="flex gap-2.5 rounded-xl border border-info bg-info-sfondo px-3.5 py-3 text-didascalia leading-[1.4] font-medium lg:hidden">
            <span aria-hidden="true" className="text-corpo font-semibold">✓</span>
            {avvisoMobile}
          </p>
        )}

        <div className="flex flex-1 items-start gap-6">
          {/* Lista dei mazzi: su desktop dentro un riquadro */}
          <section className="flex flex-1 flex-col gap-3 lg:gap-4 lg:self-stretch lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-6">
            <h2 className="hidden font-titolo text-h2 lg:block">I tuoi mazzi</h2>
            {legenda}

            {caricamento && (
              <div className="grid grid-cols-3 gap-x-2.5 gap-y-3 lg:flex lg:flex-wrap lg:gap-5">
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <Scheletro key={n} className="h-58 rounded-2xl lg:w-35" />
                ))}
              </div>
            )}
            {errore && <MessaggioErrore />}
            {mazzi?.length === 0 && <p>Non hai ancora mazzi. Creane uno in Collezione › Mazzi.</p>}

            {mazzi?.length > 0 && (
              <div className="grid grid-cols-3 gap-x-2.5 gap-y-3 lg:flex lg:flex-wrap lg:gap-5">
                {mazzi.map((mazzo) => (
                  <MazzoPerPartita
                    key={mazzo.id}
                    mazzo={mazzo}
                    selezionato={mazzo.id === idScelto}
                    onScegli={() => setIdScelto(mazzo.id)}
                  />
                ))}
              </div>
            )}
          </section>

          {/* 💻 Desktop: riepilogo a destra */}
          <div className="hidden w-85 shrink-0 lg:block">
            <Riquadro titolo="Mazzo scelto">
              {mazzoScelto ? (
                <>
                  <p className="leading-[1.4] font-semibold">{mazzoScelto.nome}</p>
                  <p>
                    {mazzoScelto.carte}/{mazzoScelto.carteMax} carte · {mazzoScelto.punti}/{mazzoScelto.puntiMax} punti
                    {mazzoScelto.scuole?.length > 0 && ` · ${mazzoScelto.scuole.join(' + ')}`}
                  </p>
                </>
              ) : (
                <p>Nessun mazzo scelto.</p>
              )}
              <Bottone className="min-h-12 w-full" onClick={conferma} disabled={!mazzoScelto || invio.inCorso}>
                {invio.inCorso ? 'Attendi…' : testoBottone}
              </Bottone>
              {invio.errore && <MessaggioErrore testo={invio.errore} />}
            </Riquadro>
          </div>
        </div>
      </div>

      {/* 📱 Mobile: barra fissa in basso con il bottone */}
      <div className="fixed inset-x-0 bottom-0 z-10 flex flex-col gap-1.5 border border-oro bg-sfondo px-5 pt-3 pb-6 lg:hidden">
        <p className="truncate leading-[1.4] font-semibold">
          {mazzoScelto ? `Scelto: ${mazzoScelto.nome}` : 'Scegli un mazzo'}
        </p>
        <Bottone onClick={conferma} disabled={!mazzoScelto || invio.inCorso}>
          {invio.inCorso ? 'Attendi…' : testoBottone}
        </Bottone>
        {invio.errore && <MessaggioErrore testo={invio.errore} />}
      </div>
    </AppLayout>
  )
}
