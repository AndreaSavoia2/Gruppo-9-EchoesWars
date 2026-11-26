import { Link } from 'react-router'

/**
 * Clickable row with icon, title, description and arrow (mobile version).
 *
 * @param {object} props - the component props
 * @param {string} props.path
 * @param {string} props.icon
 * @param {string} props.title
 * @param {string} props.description
 * @returns {JSX.Element}
 */
export default function OptionButton({ path, icon, title, description }) {
  return (
    <Link
      to={path}
      className="flex w-full items-center gap-3.5 rounded-2xl border border-gold-border bg-background px-4 py-3.5 hover:bg-background-secondary"
    >
      <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-gold">
        <img src={icon} alt="" className="size-6" />
      </span>
      <span className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
        <span className="font-semibold">{title}</span>
        <span className="text-caption font-medium text-ink-secondary">{description}</span>
      </span>
      <img src="/icons/arrow-right.svg" alt="" className="size-6 shrink-0" />
    </Link>
  )
}
