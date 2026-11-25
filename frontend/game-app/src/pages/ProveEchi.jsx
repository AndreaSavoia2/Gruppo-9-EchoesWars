import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import RigaMissione from '../components/RigaMissione'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { useUtente } from '../context/UtenteContext'
import { getProve, riscuotiProva } from '../api/progressi'

const VISIBILI = 4 // quante prove mostrare prima di "Mostra tutte"

/**
 * 13 · 13e / D10 · /missioni
 *
 * @returns {JSX.Element}
 */
export default function ProveEchi() {
  const { dati, caricamento, errore, ricarica } = useFetch(getProve)
  const { ricarica: ricaricaUtente } = useUtente()
  const [tutte, setTutte] = useState(false)
  const [esito, setEsito] = useState(null)
  const [inCorso, setInCorso] = useState(null)

  async function riscuoti(prova) {
    setInCorso(prova.id)
    try {
      const { creditiAggiunti } = await riscuotiProva(prova.id)
      setEsito({ tipo: 'successo', testo: `+${creditiAggiunti} crediti aggiunti · ${dati.completate + 1} di ${dati.totali} Prove completate.` })
      ricarica()
      ricaricaUtente()
    } catch {
      setEsito({ tipo: 'errore', testo: 'Non è stato possibile riscuotere. Riprova.' })
    }
    setInCorso(null)
  }

  const prove = dati ? (tutte ? dati.prove : dati.prove.slice(0, VISIBILI)) : []

  return (
    <AppLayout paginaAttiva="gioca">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <Intestazione titolo="Prove degli Echi" sottotitolo="12 obiettivi uguali per tutti, completabili una sola volta per account.">
          <Link to="/" className="hidden font-medium underline lg:block">
            ‹ Gioca
          </Link>
        </Intestazione>

        {caricamento && <Scheletro className="mt-3 h-80 rounded-2xl" />}
        {errore && <MessaggioErrore />}

        {dati && (
          <section className="flex flex-col gap-2.5 pt-3 lg:max-w-[610px] lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-4 lg:pt-6">
            <h2 className="hidden font-titolo text-h2 lg:block">
              Prove degli Echi · {dati.completate}/{dati.totali}
            </h2>
            {esito ? (
              <Avviso tipo={esito.tipo}>{esito.testo}</Avviso>
            ) : (
              <Avviso tipo="info" icona="">
                {dati.completate} di {dati.totali} Prove completate · {dati.creditiOttenuti} di {dati.creditiTotali} crediti ottenuti.
              </Avviso>
            )}
            <ul className="flex flex-col gap-2.5">
              {prove.map((prova) => (
                <li key={prova.id}>
                  <RigaMissione {...prova} onRiscuoti={() => riscuoti(prova)} inCorso={inCorso === prova.id} />
                </li>
              ))}
            </ul>
            {!tutte && dati.prove.length > VISIBILI && (
              <button type="button" onClick={() => setTutte(true)} className="self-start text-didascalia font-medium underline">
                Mostra tutte le {dati.prove.length} Prove
              </button>
            )}
          </section>
        )}
      </div>
    </AppLayout>
  )
}
