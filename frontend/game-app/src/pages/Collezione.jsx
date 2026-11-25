import { useCallback, useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import Bottone from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import SchedeCollezione from '../components/collezione/SchedeCollezione'
import Filtro from '../components/collezione/Filtro'
import Carta from '../components/collezione/Carta'
import { useFetch } from '../hooks/useFetch'
import { getCollezione, getFiltriCollezione } from '../api/collezione'

// I 4 filtri: "chiave" = nome usato col backend, "etichetta" = testo sul bottone
const FILTRI = [
  { chiave: 'scuola', etichetta: 'Scuola' },
  { chiave: 'eco', etichetta: 'Eco' },
  { chiave: 'rarita', etichetta: 'Rarità' },
  { chiave: 'abilita', etichetta: 'Abilità' },
]
const NESSUN_FILTRO = { scuola: [], eco: [], rarita: [], abilita: [] }

/**
 * 5 · 5f-5j · 5o / D3 · /collezione — le carte possedute, con i filtri
 *
 * @returns {JSX.Element}
 */
export default function Collezione() {
  const [filtri, setFiltri] = useState(NESSUN_FILTRO)
  const [filtroAperto, setFiltroAperto] = useState(null) // es. 'scuola', oppure null

  // Quando cambiano i filtri, cambia la funzione → useFetch rifà la richiesta
  const caricaCarte = useCallback((signal) => getCollezione(filtri, signal), [filtri])
  const { dati, caricamento, errore } = useFetch(caricaCarte)
  const { dati: opzioni } = useFetch(getFiltriCollezione)

  const filtriAttivi = Object.values(filtri).some((valori) => valori.length > 0)

  // Tasto Esc = chiudi il menu del filtro aperto
  useEffect(() => {
    if (!filtroAperto) return
    const premi = (evento) => evento.key === 'Escape' && setFiltroAperto(null)
    window.addEventListener('keydown', premi)
    return () => window.removeEventListener('keydown', premi)
  }, [filtroAperto])

  function applica(chiave, valori) {
    setFiltri({ ...filtri, [chiave]: valori })
    setFiltroAperto(null)
  }

  function azzeraTutto() {
    setFiltri(NESSUN_FILTRO)
  }

  return (
    <AppLayout paginaAttiva="collezione" sfondoMobile={null} sfondoDesktop="/images/sfondo-collezione.png">
      <div className="flex flex-col gap-3 lg:gap-6">
        {/* Intestazione: su mobile con l'immagine della biblioteca dietro */}
        <div className="relative -mx-5 -mt-8 flex h-27.5 flex-col justify-end gap-1 px-5 pb-4 lg:mx-0 lg:mt-0 lg:h-auto lg:gap-1.5 lg:p-0">
          <div aria-hidden="true" className="absolute inset-0 -z-10 lg:hidden">
            <img src="/images/sfondo-collezione.png" alt="" className="size-full object-cover" />
            <div className="absolute inset-0 bg-linear-to-b from-sfondo/5 via-sfondo/75 via-55% to-sfondo" />
          </div>
          <h1 className="font-titolo text-h2">Collezione</h1>
          {dati ? (
            <p className="text-testo-secondario lg:text-testo">{dati.totale} carte nella tua collezione</p>
          ) : (
            <Scheletro className="h-4 w-56" />
          )}
        </div>

        <div className="flex flex-col gap-3 pt-3 lg:flex-row lg:items-center lg:gap-6 lg:pt-0">
          <SchedeCollezione attiva="carte" />

          {/* Filtri. Lo "z-30" li tiene sopra il velo scuro quando un menu è aperto */}
          <div className="relative z-30 flex flex-wrap gap-2">
            {FILTRI.map(({ chiave, etichetta }) => (
              <Filtro
                key={chiave}
                etichetta={etichetta}
                opzioni={opzioni?.[chiave]}
                scelti={filtri[chiave]}
                aperto={filtroAperto === chiave}
                onApri={() => setFiltroAperto(chiave)}
                onChiudi={() => setFiltroAperto(null)}
                onApplica={(valori) => applica(chiave, valori)}
              />
            ))}
          </div>
        </div>

        {/* Velo scuro dietro il menu aperto: toccalo per chiudere */}
        {filtroAperto && (
          <button
            type="button"
            aria-label="Chiudi il menu"
            onClick={() => setFiltroAperto(null)}
            className="fixed inset-0 z-20 bg-[rgba(34,37,44,0.6)] opacity-60"
          />
        )}

        {filtriAttivi && dati && (
          <div className="flex items-center justify-between">
            <p>{dati.trovate} carte trovate</p>
            <button type="button" onClick={azzeraTutto} className="flex items-center gap-1 font-medium">
              <span aria-hidden="true">✕</span> Azzera filtri
            </button>
          </div>
        )}

        {caricamento && (
          <div className="grid grid-cols-3 gap-3 lg:grid-cols-[repeat(auto-fill,128px)] lg:gap-5">
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <Scheletro key={n} className="aspect-[107/150] rounded-lg" />
            ))}
          </div>
        )}
        {errore && <MessaggioErrore />}

        {dati?.carte.length > 0 && (
          <ul className="grid grid-cols-3 gap-3 lg:grid-cols-[repeat(auto-fill,128px)] lg:gap-5">
            {dati.carte.map((carta) => (
              <li key={carta.id}>
                <Carta carta={carta} />
              </li>
            ))}
          </ul>
        )}

        {dati?.carte.length === 0 && <NessunRisultato onAzzera={azzeraTutto} filtriAttivi={filtriAttivi} />}
      </div>
    </AppLayout>
  )
}

/**
 * 5o · Nessuna carta trovata
 *
 * @param {object} props - le proprietà del componente
 * @param {Function} props.onAzzera
 * @param {*} props.filtriAttivi
 * @returns {JSX.Element}
 */
function NessunRisultato({ onAzzera, filtriAttivi }) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-oro bg-sfondo p-5 text-center lg:max-w-md">
      <span aria-hidden="true" className="flex size-16 items-center justify-center rounded-full border border-oro bg-oro/30 text-2xl">
        🔍
      </span>
      <h2 className="font-titolo text-[22px]">Nessuna carta trovata</h2>
      <p>
        {filtriAttivi
          ? 'Nessuna carta della tua collezione corrisponde ai filtri scelti.'
          : 'La tua collezione è ancora vuota.'}
      </p>
      {filtriAttivi && (
        <Bottone variante="secondario" onClick={onAzzera}>
          Azzera filtri
        </Bottone>
      )}
    </div>
  )
}
