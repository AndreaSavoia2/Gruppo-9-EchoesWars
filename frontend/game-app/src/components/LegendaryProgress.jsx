/**
 * "Leggendaria garantita" (guaranteed legendary) counter: 10 notches = 10 packs.
 *
 * @param {object} props - the component props
 * @param {number} props.packs - packs opened in the current cycle
 * @param {number} props.total - packs needed for the guaranteed legendary
 * @returns {JSX.Element}
 */
export default function LegendaryProgress({ packs, total }) {
  const missing = total - packs
  // Creates a list [0, 1, 2, ... 9] to draw the notches
  const notches = Array.from({ length: total }, (_, i) => i)

  return (
    <div className="flex flex-col gap-2.5 rounded-2xl border border-gold-border bg-background px-4 py-3.5">
      <div className="flex justify-between leading-[1.4] font-semibold">
        <p>★ Leggendaria garantita</p>
        <p className="text-ink-secondary">
          {packs}/{total}
        </p>
      </div>

      <div className="flex gap-1" aria-hidden="true">
        {notches.map((i) => (
          <div
            key={i}
            className={`h-2 flex-1 rounded ${i < packs ? 'bg-gold-border' : 'bg-background-secondary'}`}
          />
        ))}
      </div>

      <p className="text-caption leading-[1.4] font-medium text-ink-secondary">
        Mancano {missing} buste alla prossima carta leggendaria garantita.
      </p>
    </div>
  )
}
