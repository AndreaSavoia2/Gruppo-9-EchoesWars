import { Link, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Schede from '../components/Schede'
import Avviso from '../components/Avviso'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import CartaRichiesta from '../components/amici/CartaRichiesta'
import { useFetch } from '../hooks/useFetch'
import { useAzioneRichiesta } from '../hooks/useAzioneRichiesta'
import { getRichieste } from '../api/richieste'

// R1-R6 / D15 · /richieste?scheda=tutte|amicizia|sfide
export default function Richieste() {
  const [parametri] = useSearchParams()
  const scheda = parametri.get('scheda') ?? 'tutte'
  const { dati: richieste, caricamento, errore, ricarica } = useFetch(getRichieste)
  const { messaggio, accetta, rifiuta } = useAzioneRichiesta(ricarica)

  const conta = (tipo) => richieste?.filter((r) => r.tipo === tipo).length ?? 0
  const totale = richieste?.length ?? 0
  const schede = [
    { id: 'tutte', etichetta: `Tutte (${totale})`, percorso: '/richieste' },
    { id: 'amicizia', etichetta: `Amicizia (${conta('amicizia')})`, percorso: '/richieste?scheda=amicizia' },
    { id: 'sfide', etichetta: `Sfide (${conta('sfida')})`, percorso: '/richieste?scheda=sfide' },
  ]
  const visibili = richieste?.filter((r) => scheda === 'tutte' || (scheda === 'sfide' ? r.tipo === 'sfida' : r.tipo === scheda)) ?? []

  let sottotitolo = ' '
  if (richieste) sottotitolo = totale === 0 ? 'Nessuna richiesta in attesa' : `Hai ${totale} ${totale === 1 ? 'richiesta' : 'richieste'} da gestire`

  return (
    <AppLayout paginaAttiva="gioca">
      <div className="flex flex-col gap-2.5 lg:gap-4">
        <Intestazione titolo="Richieste" sottotitolo={sottotitolo}>
          <Link to="/" className="hidden font-medium underline lg:block">
            ‹ Home
          </Link>
        </Intestazione>
        <Link to="/" className="mt-3 self-start font-medium underline lg:hidden">
          ‹ Home
        </Link>

        <Schede schede={schede} attiva={scheda} etichetta="Tipi di richiesta" className="lg:w-[480px]" />

        {messaggio && (
          <Avviso tipo={messaggio.tipo} icona={messaggio.tipo === 'info' ? '' : undefined}>
            {messaggio.testo}
          </Avviso>
        )}

        {caricamento && <Scheletro className="h-40 rounded-2xl" />}
        {errore && <MessaggioErrore />}

        {richieste && visibili.length === 0 && (
          <div className="flex flex-col items-center gap-2 rounded-2xl border border-oro bg-sfondo px-5 py-6 text-center lg:max-w-md">
            <span aria-hidden="true" className="text-2xl">📭</span>
            <h2 className="font-titolo text-[22px]">Nessuna richiesta</h2>
            <p>Qui trovi le richieste di amicizia e le sfide che ricevi.</p>
          </div>
        )}

        {visibili.length > 0 && (
          <ul className="grid gap-2.5 lg:grid-cols-2 lg:gap-3">
            {visibili.map((richiesta) => (
              <li key={richiesta.id}>
                <CartaRichiesta richiesta={richiesta} onAccetta={() => accetta(richiesta)} onRifiuta={() => rifiuta(richiesta)} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </AppLayout>
  )
}
