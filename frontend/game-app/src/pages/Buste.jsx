import { useState } from 'react'
import { useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Bottone from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import ProgressoLeggendaria from '../components/ProgressoLeggendaria'
import SchedeCollezione from '../components/collezione/SchedeCollezione'
import AperturaBusta from '../components/buste/AperturaBusta'
import { useFetch } from '../hooks/useFetch'
import { apriBuste, getBuste } from '../api/buste'

/**
 * 8 · /collezione/buste — le buste da aprire
 *
 * @returns {JSX.Element}
 */
export default function Buste() {
  const navigate = useNavigate()
  const { dati, caricamento, errore, ricarica } = useFetch(getBuste)
  const [apertura, setApertura] = useState(null) // il risultato da mostrare nell'animazione
  const [invio, setInvio] = useState({ inCorso: false, errore: null })

  async function apri(quantita) {
    setInvio({ inCorso: true, errore: null })
    try {
      const risultato = await apriBuste(quantita)
      setApertura(risultato)
      setInvio({ inCorso: false, errore: null })
      ricarica() // aggiorna il numero di buste e la leggendaria
    } catch {
      setInvio({ inCorso: false, errore: 'Non è stato possibile aprire la busta. Riprova.' })
    }
  }

  const disponibili = dati?.disponibili ?? 0

  return (
    <AppLayout paginaAttiva="collezione" sfondoMobile="/images/sfondo-buste.png" sfondoDesktop="/images/sfondo-collezione.png">
      <div className="flex flex-col items-center gap-3.5 lg:items-start lg:gap-6">
        <div className="w-full">
          <Intestazione
            titolo="Collezione"
            sottotitolo={dati ? (disponibili === 0 ? 'Non hai buste da aprire.' : `Hai ${disponibili} buste da aprire.`) : ' '}
          />
        </div>

        <div className="w-full pt-3 lg:pt-0">
          <SchedeCollezione attiva="buste" />
        </div>

        {caricamento && <Scheletro className="h-67 w-50 rounded-xl" />}
        {errore && <MessaggioErrore />}

        {dati && (
          <div className="flex w-full flex-col items-center gap-3.5 lg:max-w-md">
            {/* Busta in primo piano con il numero */}
            <div className="relative">
              <img
                src="/images/busta.png"
                alt="Busta Echoes’ War"
                className={`h-67 w-50 rounded-xl object-cover shadow-[0_8px_28px_rgba(184,138,66,0.45)] ${disponibili === 0 ? 'opacity-40' : ''}`}
              />
              {disponibili > 0 && (
                <span className="absolute -top-2 -right-1 rounded-full bg-info px-2.5 py-1 leading-[1.4] font-semibold text-sfondo">
                  ×{disponibili}
                  <span className="sr-only"> buste</span>
                </span>
              )}
            </div>

            <div className="w-full">
              <ProgressoLeggendaria buste={dati.leggendaria.buste} totale={dati.leggendaria.totale} />
            </div>

            <Bottone onClick={() => apri(1)} disabled={disponibili === 0 || invio.inCorso}>
              {invio.inCorso ? 'Apertura…' : 'Apri busta'}
            </Bottone>
            {disponibili > 1 && (
              <Bottone variante="secondario" onClick={() => apri(disponibili)} disabled={invio.inCorso}>
                Apri tutte ({disponibili})
              </Bottone>
            )}
            {invio.errore && <MessaggioErrore testo={invio.errore} />}
          </div>
        )}
      </div>

      {apertura && (
        <AperturaBusta
          carte={apertura.carte}
          riepilogo={apertura.riepilogo}
          onFine={() => navigate('/collezione')}
        />
      )}
    </AppLayout>
  )
}
