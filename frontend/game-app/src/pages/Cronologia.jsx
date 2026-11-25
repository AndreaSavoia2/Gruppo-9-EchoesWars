import { useCallback } from 'react'
import { Link, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Schede from '../components/Schede'
import RigaPartita from '../components/RigaPartita'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { getCronologia } from '../api/progressi'

const schede = [
  { id: 'tutte', etichetta: 'Tutte', percorso: '/cronologia' },
  { id: 'vittoria', etichetta: 'Vittorie', percorso: '/cronologia?esito=vittoria' },
  { id: 'sconfitta', etichetta: 'Sconfitte', percorso: '/cronologia?esito=sconfitta' },
  { id: 'pareggio', etichetta: 'Pareggi', percorso: '/cronologia?esito=pareggio' },
]

// 2g / D13 · /cronologia?esito=vittoria|sconfitta|pareggio
export default function Cronologia() {
  const [parametri] = useSearchParams()
  const esito = parametri.get('esito')
  const carica = useCallback((signal) => getCronologia(esito, signal), [esito])
  const { dati: partite, caricamento, errore } = useFetch(carica)

  return (
    <AppLayout paginaAttiva="gioca">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <Intestazione titolo="Cronologia partite" sottotitolo="Le tue ultime sfide." />
        <Link to="/" className="mt-4 flex min-h-11 items-center self-start font-medium text-testo-secondario lg:hidden">
          ‹ Gioca
        </Link>

        <section className="flex flex-col gap-2.5 lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-4">
          <Link to="/" className="hidden min-h-11 items-center self-start font-medium text-testo-secondario lg:flex">
            ‹ Gioca
          </Link>
          <Schede schede={schede} attiva={esito ?? 'tutte'} etichetta="Filtra per esito" className="" />

          {caricamento && <Scheletro className="h-60 rounded-2xl" />}
          {errore && <MessaggioErrore />}
          {partite?.length === 0 && <p>Nessuna partita da mostrare.</p>}

          {partite?.length > 0 && (
            <ul className="flex flex-col gap-2.5">
              {partite.map((partita) => (
                <li key={partita.id}>
                  <RigaPartita {...partita} conVs percorso={`/partita/${partita.id}/dettagli`} />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </AppLayout>
  )
}
