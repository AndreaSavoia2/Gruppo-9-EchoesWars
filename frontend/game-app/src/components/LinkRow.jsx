import { Link } from 'react-router'

/**
 * Clickable Profile row: icon in a gold square, title, detail, arrow.
 *
 * @param {object} props - the component props
 * @param {string} props.path
 * @param {string} props.icon
 * @param {string} props.title
 * @param {string} props.detail
 * @returns {JSX.Element}
 */
export default function LinkRow({ path, icon, title, detail }) {
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
        <span className="text-caption font-medium text-ink-secondary">{detail}</span>
      </span>
      <img src="/icons/arrow-right.svg" alt="" className="size-6 shrink-0" />
    </Link>
  )
}
