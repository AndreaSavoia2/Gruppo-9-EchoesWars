import { Link } from 'react-router'

/**
 * Small rounded button (Requests, Missions, History).
 * notification = true → shows the purple dot at the top right.
 *
 * @param {object} props - the component props
 * @param {string} props.text
 * @param {string} props.path
 * @param {boolean} [props.notification=false]
 * @returns {JSX.Element}
 */
export default function Chip({ text, path, notification = false }) {
  return (
    <Link
      to={path}
      className="relative flex min-h-11 items-center justify-center rounded-full border border-gold-border bg-gold px-4.5 font-medium hover:brightness-95"
    >
      {text}
      {notification && (
        <>
          <img src="/icons/notification-dot.svg" alt="" className="absolute -top-1.5 right-0 size-3.5" />
          <span className="sr-only"> (novità)</span>
        </>
      )}
    </Link>
  )
}
