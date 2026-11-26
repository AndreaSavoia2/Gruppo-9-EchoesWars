/**
 * Title (Cinzel) + subtitle at the top of the app screens.
 * children: extra things under the subtitle (e.g. the "‹ Home" link on desktop)
 *
 * @param {object} props - the component props
 * @param {string} props.title
 * @param {string} [props.subtitle]
 * @param {React.ReactNode} [props.children]
 * @returns {JSX.Element}
 */
export default function PageHeader({ title, subtitle, children }) {
  return (
    <div className="flex flex-col items-start gap-1 lg:gap-1.5">
      <h1 className="font-title text-h2">{title}</h1>
      {subtitle && <p>{subtitle}</p>}
      {children}
    </div>
  )
}
