/**
 * Container with a title (e.g. "Ultime partite", "Prove degli Echi").
 *
 * @param {object} props - the component props
 * @param {string} props.title
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function Panel({ title, children }) {
  return (
    <section className="flex flex-col items-start gap-3 rounded-2xl border border-gold bg-background p-5">
      <h2 className="font-title text-h2">{title}</h2>
      {children}
    </section>
  )
}
