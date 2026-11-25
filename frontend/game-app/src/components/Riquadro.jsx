/**
 * Contenitore con titolo (es. "Ultime partite", "Prove degli Echi").
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.titolo
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function Riquadro({ titolo, children }) {
  return (
    <section className="flex flex-col items-start gap-3 rounded-2xl border border-oro bg-sfondo p-5">
      <h2 className="font-titolo text-h2">{titolo}</h2>
      {children}
    </section>
  )
}
