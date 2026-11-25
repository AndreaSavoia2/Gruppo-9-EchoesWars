/**
 * Contatore "Leggendaria garantita": 10 tacche = 10 buste.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.buste
 * @param {*} props.totale
 * @returns {JSX.Element}
 */
export default function ProgressoLeggendaria({ buste, totale }) {
  const mancano = totale - buste
  // Crea una lista [0, 1, 2, ... 9] per disegnare le tacche
  const tacche = Array.from({ length: totale }, (_, i) => i)

  return (
    <div className="flex flex-col gap-2.5 rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5">
      <div className="flex justify-between leading-[1.4] font-semibold">
        <p>★ Leggendaria garantita</p>
        <p className="text-testo-secondario">
          {buste}/{totale}
        </p>
      </div>

      <div className="flex gap-1" aria-hidden="true">
        {tacche.map((i) => (
          <div
            key={i}
            className={`h-2 flex-1 rounded ${i < buste ? 'bg-oro-bordo' : 'bg-sfondo-secondario'}`}
          />
        ))}
      </div>

      <p className="text-didascalia leading-[1.4] font-medium text-testo-secondario">
        Mancano {mancano} buste alla prossima carta leggendaria garantita.
      </p>
    </div>
  )
}
