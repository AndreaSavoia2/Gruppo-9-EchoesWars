import { useCallback, useState } from 'react'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Schede from '../components/Schede'
import Avviso from '../components/Avviso'
import Modale from '../components/Modale'
import Bottone from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import AvatarIniziali from '../components/amici/AvatarIniziali'
import CartaRichiesta from '../components/amici/CartaRichiesta'
import { useFetch } from '../hooks/useFetch'
import { useAzioneRichiesta } from '../hooks/useAzioneRichiesta'
import { aggiungiAmico, eliminaAmico, getAmiciLista } from '../api/amici'
import { getRichieste } from '../api/richieste'

// 9 · 9b · 9c / D8 · /amici
export default function Amici() {
  const { dati: amici, caricamento, errore, ricarica } = useFetch(getAmiciLista)
  const { dati: richieste, ricarica: ricaricaRichieste } = useFetch(getRichieste)
  const richiesteAmicizia = richieste?.filter((r) => r.tipo === 'amicizia') ?? []

  // Quando accetti un'amicizia cambiano sia gli amici sia le richieste
  const ricaricaTutto = useCallback(() => {
    ricarica()
    ricaricaRichieste()
  }, [ricarica, ricaricaRichieste])
  const azioni = useAzioneRichiesta(ricaricaTutto)

  const [nome, setNome] = useState('')
  const [esitoAggiungi, setEsitoAggiungi] = useState(null)
  const [daEliminare, setDaEliminare] = useState(null)
  const chiudiElimina = useCallback(() => setDaEliminare(null), [])

  async function aggiungi(evento) {
    evento.preventDefault() // il form non deve ricaricare la pagina
    const pulito = nome.trim()
    if (!pulito) return
    const conChiocciola = pulito.startsWith('@') ? pulito : `@${pulito}`
    try {
      await aggiungiAmico(conChiocciola)
      setEsitoAggiungi({ tipo: 'successo', testo: `Richiesta di amicizia inviata a ${conChiocciola}.` })
      setNome('')
    } catch {
      setEsitoAggiungi({ tipo: 'errore', testo: `Non abbiamo trovato ${conChiocciola}. Controlla il nome utente.` })
    }
  }

  async function elimina() {
    await eliminaAmico(daEliminare.id)
    setDaEliminare(null)
    ricarica()
  }

  const numeroAmici = amici?.length ?? 0
  const sottotitolo = amici && richieste ? `${numeroAmici} amici · ${richiesteAmicizia.length} richieste` : ' '

  const formAggiungi = (
    <form onSubmit={aggiungi} className="flex items-center gap-2">
      <label className="sr-only" htmlFor="cerca-amico">
        Nome utente da aggiungere
      </label>
      <input
        id="cerca-amico"
        type="text"
        value={nome}
        onChange={(evento) => setNome(evento.target.value)}
        placeholder="Cerca per nome utente"
        className="min-h-12 min-w-0 flex-1 rounded-[14px] border-[1.5px] border-testo bg-sfondo px-3.5 placeholder:text-neutro"
      />
      <Bottone type="submit" className="min-h-12 w-28 shrink-0">
        Aggiungi
      </Bottone>
    </form>
  )

  return (
    <AppLayout paginaAttiva="amici">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <Intestazione titolo="Amici" sottotitolo={sottotitolo} />

        <div className="flex flex-col gap-6 lg:flex-row lg:items-start">
          <section className="flex flex-1 flex-col gap-2.5 pt-3 lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-4 lg:pt-4">
            <h2 className="hidden font-titolo text-h2 lg:block">I tuoi amici ({numeroAmici})</h2>
            {formAggiungi}
            {esitoAggiungi && <Avviso tipo={esitoAggiungi.tipo}>{esitoAggiungi.testo}</Avviso>}

            {/* 📱 Mobile: schede Amici / Richieste */}
            <div className="lg:hidden">
              <Schede
                schede={[
                  { id: 'amici', etichetta: `Amici (${numeroAmici})`, percorso: '/amici' },
                  { id: 'richieste', etichetta: `Richieste (${richiesteAmicizia.length})`, percorso: '/richieste?scheda=amicizia' },
                ]}
                attiva="amici"
                etichetta="Amici e richieste"
              />
            </div>

            {caricamento && <Scheletro className="h-60 rounded-2xl" />}
            {errore && <MessaggioErrore />}

            {amici?.length === 0 && (
              <div className="flex flex-col items-center gap-3 rounded-2xl border border-oro bg-sfondo px-5 py-6 text-center">
                <span aria-hidden="true" className="flex size-16 items-center justify-center rounded-full border-2 border-oro bg-sfondo-secondario text-2xl">
                  👥
                </span>
                <h3 className="font-titolo text-[22px]">Non hai ancora amici</h3>
                <p>Scrivi il nome utente di un giocatore nel campo qui sopra e premi Aggiungi. Con gli amici puoi sfidarti e scambiare carte.</p>
              </div>
            )}

            {amici?.length > 0 && (
              <ul className="flex flex-col gap-2.5">
                {amici.map((amico) => (
                  <li key={amico.id} className="flex items-center gap-3 rounded-2xl border border-oro-bordo bg-sfondo py-3 pr-2.5 pl-3.5">
                    <AvatarIniziali nome={amico.nome} />
                    <span className="flex-1 truncate leading-[1.4] font-semibold">{amico.nome}</span>
                    <button
                      type="button"
                      onClick={() => setDaEliminare(amico)}
                      aria-label={`Elimina ${amico.nome} dagli amici`}
                      className="flex size-11 shrink-0 items-center justify-center rounded-full border-[1.5px] border-errore hover:bg-errore-sfondo"
                    >
                      <img src="/icons/cestino.svg" alt="" className="size-6" />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {/* 💻 Desktop: richieste di amicizia a destra */}
          <aside className="hidden w-100 shrink-0 flex-col gap-3 rounded-2xl border border-oro bg-sfondo p-4 lg:flex">
            <h2 className="font-titolo text-h2">Richieste ({richiesteAmicizia.length})</h2>
            {azioni.messaggio && <Avviso tipo={azioni.messaggio.tipo}>{azioni.messaggio.testo}</Avviso>}
            {richiesteAmicizia.length === 0 && <p>Nessuna richiesta di amicizia.</p>}
            {richiesteAmicizia.map((richiesta) => (
              <CartaRichiesta
                key={richiesta.id}
                richiesta={richiesta}
                compatta
                onAccetta={() => azioni.accetta(richiesta)}
                onRifiuta={() => azioni.rifiuta(richiesta)}
              />
            ))}
          </aside>
        </div>
      </div>

      {/* 9b · Elimina amico */}
      {daEliminare && (
        <Modale titolo={`Eliminare ${daEliminare.nome} dagli amici?`} onChiudi={chiudiElimina}>
          <p className="text-testo-secondario">
            Non potrete più sfidarvi né scambiarvi carte. Potrai inviargli di nuovo una richiesta.
          </p>
          <Bottone variante="distruttivo" onClick={elimina}>
            Elimina amico
          </Bottone>
          <Bottone variante="secondario" onClick={chiudiElimina}>
            Annulla
          </Bottone>
        </Modale>
      )}
    </AppLayout>
  )
}
