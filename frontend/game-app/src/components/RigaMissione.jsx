import BarraProgresso from './BarraProgresso'

/**
 * Una "Prova degli Echi" (dal Figma "App/Riga missione").
 * Stati: In corso · Completata (bottone Riscuoti) · Riscossa (✓ verde)
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.titolo
 * @param {*} props.ricompensa
 * @param {*} props.fatto
 * @param {*} props.totale
 * @param {boolean} [props.riscossa=false]
 * @param {Function} props.onRiscuoti
 * @param {boolean} [props.inCorso=false]
 * @returns {JSX.Element}
 */
export default function RigaMissione({ titolo, ricompensa, fatto, totale, riscossa = false, onRiscuoti, inCorso = false }) {
  const completata = fatto >= totale
  return (
    <div
      className={`flex w-full items-center gap-3 rounded-2xl border border-oro-bordo py-3.5 pr-3 pl-4 leading-[1.4] ${
        riscossa ? 'bg-sfondo-secondario' : 'bg-sfondo'
      }`}
    >
      <div className="flex min-w-0 flex-1 flex-col gap-1.5">
        <p className="font-semibold">{titolo}</p>
        <p className="text-didascalia font-medium text-testo-secondario">Ricompensa: {ricompensa}</p>
        <BarraProgresso fatto={fatto} totale={totale} />
      </div>
      {riscossa && <span className="shrink-0 font-semibold text-successo">✓ Riscossa</span>}
      {completata && !riscossa && onRiscuoti && (
        <button
          type="button"
          onClick={onRiscuoti}
          disabled={inCorso}
          className="flex min-h-11 shrink-0 items-center rounded-full bg-oro-bordo px-5 font-medium disabled:opacity-50"
        >
          Riscuoti
        </button>
      )}
    </div>
  )
}
