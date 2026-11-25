import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { getAccessi } from '../api/progressi'

/**
 * 14 / D14 · /accessi — calendario degli accessi consecutivi
 *
 * @returns {JSX.Element}
 */
export default function Accessi() {
  const { dati, caricamento, errore } = useFetch(getAccessi)

  const ricompensaDel = (giorno) => dati?.ricompense.find((r) => r.giorno === giorno)
  const prossima = dati?.ricompense.find((r) => r.giorno > dati.giorno)

  return (
    <AppLayout sfondoMobile={null}>
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <Intestazione titolo="Accessi consecutivi" sottotitolo={dati ? `Giorno ${dati.giorno} di ${dati.totale}` : ' '}>
          <p className="hidden lg:block">
            Ogni 5 giorni consecutivi di accesso sblocchi una ricompensa. Se salti un giorno, il conteggio riparte dal giorno 1; dopo
            il giorno 30 il ciclo ricomincia.
          </p>
          <Link to="/profilo" className="hidden font-medium underline lg:block">
            ‹ Profilo
          </Link>
        </Intestazione>
        <Link to="/profilo" className="mt-3 self-start font-medium underline lg:hidden">
          ‹ Profilo
        </Link>
        <p className="text-didascalia leading-[1.4] font-medium lg:hidden">
          Ogni 5 giorni consecutivi sblocchi una ricompensa. Se salti un giorno, riparti dal giorno 1.
        </p>

        {caricamento && <Scheletro className="h-80 rounded-2xl" />}
        {errore && <MessaggioErrore />}

        {dati && (
          <div className="flex flex-col gap-2.5 lg:flex-row lg:items-start lg:gap-6">
            <section className="flex flex-col gap-2.5 lg:flex-1 lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-6">
              <h2 className="hidden font-titolo text-h2 lg:block">
                Giorno {dati.giorno} di {dati.totale}
              </h2>
              <ol className="grid grid-cols-6 gap-2.5 lg:grid-cols-8">
                {Array.from({ length: dati.totale }, (_, i) => i + 1).map((giorno) => (
                  <li key={giorno}>
                    <Giorno numero={giorno} oggi={dati.giorno} regalo={Boolean(ricompensaDel(giorno))} />
                  </li>
                ))}
              </ol>
              <p className="text-didascalia leading-[1.4] font-medium">✓ Accesso fatto &nbsp; ▢ Oggi (bordo spesso) &nbsp; 🎁 Giorno con ricompensa</p>
            </section>

            <div className="flex flex-col gap-4 lg:w-100 lg:shrink-0">
              {prossima && (
                <section className="flex flex-col gap-1 rounded-2xl border-2 border-oro-bordo bg-sfondo px-4 py-3.5 leading-[1.4]">
                  <h2 className="font-semibold lg:text-didascalia">
                    <span className="lg:hidden">Prossima: 🎁 {prossima.nome}</span>
                    <span className="hidden lg:inline">Prossima ricompensa</span>
                  </h2>
                  <p className="hidden font-titolo text-h2 leading-normal lg:block">🎁 {prossima.nome}</p>
                  <p className="text-didascalia font-medium">
                    Al giorno {prossima.giorno} · mancano {prossima.giorno - dati.giorno} accessi
                  </p>
                </section>
              )}

              {/* 💻 Desktop: tutte le ricompense del ciclo */}
              <section className="hidden flex-col gap-2 rounded-2xl border border-oro bg-sfondo p-4 lg:flex">
                <h2 className="font-titolo text-h2">Ricompense del ciclo</h2>
                <ul className="flex flex-col gap-2">
                  {dati.ricompense.map((r) => (
                    <li
                      key={r.giorno}
                      className={`flex items-center justify-between rounded-lg px-2.5 py-2 ${r.sbloccata ? 'bg-successo-sfondo' : 'bg-sfondo-secondario'}`}
                    >
                      <span className="font-semibold">Giorno {r.giorno}</span>
                      <span className={`text-didascalia ${r.sbloccata ? 'text-successo' : ''}`}>
                        {r.sbloccata ? `✓ ${r.nome} · sbloccata` : r.nome}
                      </span>
                    </li>
                  ))}
                </ul>
              </section>
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * Un giorno del calendario (dal Figma "App/Giorno accesso"): Fatto · Oggi · Futuro (🎁 se c'è una ricompensa)
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.numero
 * @param {*} props.oggi
 * @param {*} props.regalo
 * @returns {JSX.Element}
 */
function Giorno({ numero, oggi, regalo }) {
  const fatto = numero < oggi
  const eOggi = numero === oggi
  let stile = 'border border-oro bg-sfondo-secondario'
  if (fatto) stile = 'border border-successo bg-successo-sfondo'
  if (eOggi) stile = 'border-3 border-testo bg-sfondo'

  return (
    <span
      aria-current={eOggi ? 'date' : undefined}
      className={`flex aspect-square w-full flex-col items-center justify-center rounded-xl leading-[1.4] font-semibold lg:size-11.5 ${stile}`}
    >
      {fatto && <span className="text-successo" aria-hidden="true">✓</span>}
      {!fatto && !eOggi && regalo && <span aria-hidden="true">🎁</span>}
      {numero}
      {eOggi && <span className="text-didascalia font-medium">Oggi</span>}
      {fatto && <span className="sr-only">, fatto</span>}
      {regalo && !fatto && <span className="sr-only">, ricompensa</span>}
    </span>
  )
}
