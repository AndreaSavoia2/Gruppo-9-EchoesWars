import { useCallback, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import Bottone, { BottoneLink } from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import CoppiaCarte from '../components/scambi/CoppiaCarte'
import { useFetch } from '../hooks/useFetch'
import { accettaScambio, getScambio, rifiutaScambio } from '../api/scambi'
import { REGOLE_SCAMBI } from '../data/regole'

// 10h · 10i · /scambi/:id — conferma di una proposta ricevuta, poi "Scambio completato"
export default function ConfermaScambio() {
  const { id } = useParams()
  const navigate = useNavigate()
  const carica = useCallback((signal) => getScambio(id, signal), [id])
  const { dati: proposta, caricamento, errore } = useFetch(carica)
  const [completato, setCompletato] = useState(false)
  const [azione, setAzione] = useState({ inCorso: false, errore: null })

  async function esegui(funzione, dopo) {
    setAzione({ inCorso: true, errore: null })
    try {
      await funzione(id)
      dopo()
    } catch {
      setAzione({ inCorso: false, errore: 'Operazione non riuscita. Riprova.' })
    }
  }

  return (
    <AppLayout paginaAttiva="scambi">
      <div className="flex flex-col gap-3 lg:max-w-2xl lg:gap-4">
        {caricamento && <Scheletro className="h-80 rounded-2xl" />}
        {errore && <MessaggioErrore testo="Impossibile caricare la proposta." />}

        {proposta && !completato && (
          <>
            <Intestazione titolo="Conferma scambio" sottotitolo={`Proposta di ${proposta.amico.nome}`} />
            <div className="mt-6 flex flex-col gap-3">
              <Avviso tipo="info" barra>
                Dopo questo scambio dovrai aspettare {REGOLE_SCAMBI.orePausa} ore per il prossimo.
              </Avviso>
              <div className="rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5">
                <CoppiaCarte
                  sinistra={{ etichetta: 'Ricevi', carta: proposta.offerta, allinea: 'start' }}
                  destra={{ etichetta: 'Dai', carta: proposta.richiesta, allinea: 'end' }}
                  compatta
                />
              </div>
              <div className="flex gap-2.5">
                <Bottone onClick={() => esegui(accettaScambio, () => setCompletato(true))} disabled={azione.inCorso} className="flex-1">
                  Accetta scambio
                </Bottone>
                <Bottone variante="secondario" onClick={() => esegui(rifiutaScambio, () => navigate('/scambi'))} disabled={azione.inCorso} className="flex-1">
                  Rifiuta
                </Bottone>
              </div>
              {azione.errore && <MessaggioErrore testo={azione.errore} />}
            </div>
          </>
        )}

        {proposta && completato && (
          <>
            <Intestazione titolo="Scambio completato" sottotitolo="Le carte sono nelle vostre collezioni." />
            <div className="mt-6 flex flex-col gap-3" aria-live="polite">
              <Avviso tipo="successo" barra>
                Scambio completato. Gli scambi si fermano per {REGOLE_SCAMBI.orePausa} ore: potrai farne un altro dopo.
              </Avviso>
              <div className="rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5">
                <CoppiaCarte
                  sinistra={{ etichetta: 'Ora è tua', carta: proposta.offerta, allinea: 'start' }}
                  destra={{ etichetta: `Ora è di ${proposta.amico.nome}`, carta: proposta.richiesta, allinea: 'end' }}
                  simbolo="✓"
                  compatta
                />
              </div>
              <BottoneLink to="/scambi">Torna agli scambi</BottoneLink>
            </div>
          </>
        )}
      </div>
    </AppLayout>
  )
}
