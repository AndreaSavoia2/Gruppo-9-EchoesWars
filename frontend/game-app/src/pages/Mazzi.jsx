import { useCallback, useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import { BottoneLink } from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import MazzoPerPartita from '../components/MazzoPerPartita'
import SchedeCollezione from '../components/collezione/SchedeCollezione'
import Filtro from '../components/collezione/Filtro'
import { useFetch } from '../hooks/useFetch'
import { getFiltriMazzi, getMazzi } from '../api/mazzi'

const FILTRI = [
  { chiave: 'scuola', etichetta: 'Scuola' },
  { chiave: 'eco', etichetta: 'Eco' },
  { chiave: 'ordina', etichetta: 'Ordina', titolo: 'Ordina per', singola: true },
]
const NESSUN_FILTRO = { scuola: [], eco: [], ordina: [] }

/**
 * 5c · 5k · 5l · 5m · 5p / D4 · /collezione/mazzi — i tuoi mazzi
 *
 * @returns {JSX.Element}
 */
export default function Mazzi() {
  const [filtri, setFiltri] = useState(NESSUN_FILTRO)
  const [filtroAperto, setFiltroAperto] = useState(null)

  const caricaMazzi = useCallback((signal) => getMazzi(signal, filtri), [filtri])
  const { dati, caricamento, errore } = useFetch(caricaMazzi)
  const { dati: opzioni } = useFetch(getFiltriMazzi)

  // Tasto Esc = chiudi il menu del filtro aperto
  useEffect(() => {
    if (!filtroAperto) return
    const premi = (evento) => evento.key === 'Escape' && setFiltroAperto(null)
    window.addEventListener('keydown', premi)
    return () => window.removeEventListener('keydown', premi)
  }, [filtroAperto])

  const pieno = dati && dati.totale >= dati.massimo
  const nessunMazzo = dati?.totale === 0

  return (
    <AppLayout paginaAttiva="collezione" sfondoMobile="/images/sfondo-mazzo.png" sfondoDesktop="/images/sfondo-mazzo-desktop.png">
      <div className="flex flex-col gap-3 lg:gap-6">
        <Intestazione titolo="Collezione" sottotitolo="Modifica o crea i tuoi mazzi." />

        <div className="flex flex-col gap-3 pt-3 lg:flex-row lg:items-center lg:gap-6 lg:pt-0">
          <SchedeCollezione attiva="mazzi" />

          {/* 📱 Mobile: il contatore sta tra le schede e i filtri */}
          {dati && (
            <p className="leading-[1.4] font-semibold lg:hidden">
              {dati.totale} di {dati.massimo} mazzi
            </p>
          )}

          {!nessunMazzo && (
            <div className="relative z-30 flex flex-wrap gap-2">
              {FILTRI.map(({ chiave, etichetta, titolo, singola }) => (
                <Filtro
                  key={chiave}
                  etichetta={etichetta}
                  titolo={titolo}
                  singola={singola}
                  opzioni={opzioni?.[chiave]}
                  scelti={filtri[chiave]}
                  aperto={filtroAperto === chiave}
                  onApri={() => setFiltroAperto(chiave)}
                  onChiudi={() => setFiltroAperto(null)}
                  onApplica={(valori) => {
                    setFiltri({ ...filtri, [chiave]: valori })
                    setFiltroAperto(null)
                  }}
                />
              ))}
            </div>
          )}

          {!nessunMazzo && (
            <BottoneLink
              to="/collezione/mazzi/nuovo"
              aria-disabled={pieno}
              onClick={(evento) => pieno && evento.preventDefault()}
              className={`w-full lg:ml-auto lg:w-auto lg:shadow-[0_6px_8px_rgba(0,0,0,0.15)] ${pieno ? 'pointer-events-none opacity-50' : ''}`}
            >
              + Crea mazzo
            </BottoneLink>
          )}
        </div>

        {filtroAperto && (
          <button
            type="button"
            aria-label="Chiudi il menu"
            onClick={() => setFiltroAperto(null)}
            className="fixed inset-0 z-20 bg-[rgba(34,37,44,0.6)] opacity-60"
          />
        )}

        {/* 💻 Desktop: contatore sopra la griglia */}
        {dati && !nessunMazzo && (
          <p className="hidden leading-[1.4] font-semibold lg:block">
            {dati.totale} di {dati.massimo} mazzi
            {!pieno && ` · puoi crearne ancora ${dati.massimo - dati.totale}`}
          </p>
        )}

        {caricamento && (
          <div className="grid grid-cols-3 gap-x-2.5 gap-y-5 lg:flex lg:flex-wrap">
            {[1, 2, 3].map((n) => (
              <Scheletro key={n} className="h-58 rounded-2xl lg:w-35" />
            ))}
          </div>
        )}
        {errore && <MessaggioErrore />}

        {dati?.mazzi.length > 0 && (
          <ul className="grid grid-cols-3 gap-x-2.5 gap-y-5 lg:flex lg:flex-wrap lg:gap-5">
            {dati.mazzi.map((mazzo) => (
              <li key={mazzo.id}>
                <MazzoPerPartita mazzo={mazzo} percorso={`/collezione/mazzi/${mazzo.id}`} />
              </li>
            ))}
          </ul>
        )}

        {/* Ci sono mazzi, ma nessuno rispetta i filtri */}
        {dati && !nessunMazzo && dati.mazzi.length === 0 && <p>Nessun mazzo corrisponde ai filtri scelti.</p>}

        {nessunMazzo && <NessunMazzo />}

        {dati && !nessunMazzo && (
          <p className="text-center text-didascalia lg:hidden">Puoi avere fino a {dati.massimo} mazzi.</p>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * 5p · Non hai ancora un mazzo
 *
 * @returns {JSX.Element}
 */
function NessunMazzo() {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-oro bg-sfondo px-5 py-7 text-center lg:max-w-md">
      <span aria-hidden="true" className="flex size-20 items-center justify-center rounded-full border-2 border-oro bg-sfondo-secondario font-titolo text-h2">
        🂠
      </span>
      <h2 className="font-titolo text-[22px]">Non hai ancora un mazzo</h2>
      <p>Un mazzo ha 30 carte di 2 Echi e costa al massimo 98 punti. Ti serve per giocare.</p>
      <BottoneLink to="/collezione/mazzi/nuovo" className="w-full">
        Crea il tuo primo mazzo
      </BottoneLink>
    </div>
  )
}
