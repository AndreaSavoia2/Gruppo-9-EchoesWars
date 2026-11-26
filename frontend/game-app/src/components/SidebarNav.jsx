import { Link } from 'react-router'
import { menuItems } from '../data/menu'

/**
 * Side menu: visible ONLY on desktop.
 * "hidden" = hidden · "lg:flex" = visible from 1024px up
 *
 * @param {object} props - the component props
 * @param {string} props.activePage - id of the current menu entry
 * @returns {JSX.Element}
 */
export default function SidebarNav({ activePage }) {
  return (
    <nav
      aria-label="Menu principale"
      className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col gap-2 border-r border-gold bg-background px-4 py-7 lg:flex"
    >
      <p className="mb-6 font-title text-h2">Echoes&apos; War</p>

      <ul className="flex flex-col gap-1.5">
        {menuItems.map((entry) => {
          const active = entry.id === activePage
          return (
            <li key={entry.id}>
              <Link
                to={entry.path}
                aria-current={active ? 'page' : undefined}
                className={`flex h-12 items-center gap-3 rounded-xl px-3.5 ${
                  active ? 'bg-gold-active font-semibold' : 'font-medium hover:bg-gold/30'
                }`}
              >
                {/* Small vertical bar: only on the active entry */}
                {active && <span className="h-6 w-1 rounded-sm bg-ink" />}
                <img src={entry.icon} alt="" className="size-6 object-cover" />
                {entry.label}
              </Link>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
