import { BottoneLink } from '../Bottone'

// Come appare ogni esito (dal Figma G5 / G5b)
const esiti = {
  vittoria: {
    titolo: 'Hai vinto!',
    icona: '🏆',
    etichetta: 'Vittoria',
    stile: 'border-successo bg-successo-sfondo text-successo',
  },
  // Non c'è nel Figma: stessa struttura, colore neutro grigio (può capitare: stessi PV a fine partita)
  pareggio: {
    titolo: 'Pareggio',
    icona: '🤝',
    etichetta: 'Pareggio',
    stile: 'border-neutro bg-sfondo-secondario text-neutro',
  },
  sconfitta: {
    titolo: 'Hai perso',
    icona: '✕',
    etichetta: 'Sconfitta',
    stile: 'border-errore bg-errore-sfondo text-errore',
  },
}

/**
 * G5 / G5b / DG4 / DG4b · Risultato della partita
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.partita
 * @returns {JSX.Element}
 */
export default function Risultato({ partita }) {
  const esito = esiti[partita.esito]

  const numeri = [
    { valore: `${partita.pvMiei} – ${partita.pvAvversario}`, etichetta: 'PV finali' },
    { valore: `+${partita.creditiGuadagnati}`, etichetta: 'Crediti' },
    { valore: partita.round, etichetta: 'Round' },
  ]

  return (
    <div className="flex flex-col items-center gap-4 pt-2 lg:items-start lg:gap-6 lg:pt-0">
      <div className="flex flex-col items-center gap-4 lg:items-start lg:gap-1.5">
        <h1 className="font-titolo text-h2">{esito.titolo}</h1>
        <p>
          contro {partita.avversario.nome} · {partita.round} round
        </p>
      </div>

      <div className="flex w-full flex-col items-center gap-4 lg:flex-row lg:gap-6">
        {/* Cerchio con l'esito: icona + parola (non solo il colore) */}
        <div
          className={`flex size-30 shrink-0 flex-col items-center justify-center rounded-full border-3 lg:size-35 ${esito.stile}`}
        >
          <span aria-hidden="true" className="font-titolo text-[28px]">
            {esito.icona}
          </span>
          <span className="sr-only lg:not-sr-only lg:leading-[1.4] lg:font-semibold">{esito.etichetta}</span>
        </div>

        {/* 3 riquadri con i numeri */}
        <dl className="flex w-full gap-3 lg:flex-1 lg:gap-4">
          {numeri.map((n) => (
            <div
              key={n.etichetta}
              className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-[14px] border border-oro bg-sfondo px-2 py-3 lg:rounded-2xl lg:p-6"
            >
              <dt className="text-didascalia leading-[1.4] font-medium">{n.etichetta}</dt>
              <dd className="font-titolo text-h2 lg:text-[36px]">{n.valore}</dd>
            </div>
          ))}
        </dl>
      </div>

      <div className="flex w-full flex-col gap-4 lg:w-auto lg:flex-row">
        <BottoneLink to={`/partita/${partita.id}/dettagli`} className="w-full lg:min-h-12 lg:w-50">
          Vedi i dettagli
        </BottoneLink>
        <BottoneLink to="/" variante="secondario" className="w-full lg:min-h-12 lg:w-50">
          Torna alla home
        </BottoneLink>
      </div>
    </div>
  )
}
