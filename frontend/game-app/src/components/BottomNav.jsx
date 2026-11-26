import { Link } from 'react-router'
import { menuItems, mobileOrder } from '../data/menu'

/**
 * Bottom menu: visible ONLY on mobile.
 * "lg:hidden" = it disappears from 1024px up
 *
 * @param {object} props - the component props
 * @param {string} props.activePage - id of the current menu entry
 * @returns {JSX.Element}
 */
export default function BottomNav({ activePage }) {
  // Put the entries back in the mobile order (Play in the middle)
  const entries = mobileOrder.map((id) => menuItems.find((entry) => entry.id === id))

  return (
    <nav
      aria-label="Menu principale"
      className="fixed inset-x-0 bottom-0 z-10 border-t border-gold bg-background-secondary px-1 pb-2 sm:px-3 lg:hidden"
    >
      <ul className="flex h-20 items-center justify-between">
        {entries.map((entry) => {
          const active = entry.id === activePage
          return (
            <li key={entry.id}>
              <Link
                to={entry.path}
                aria-current={active ? 'page' : undefined}
                className={`flex h-14 min-w-14 flex-col items-center justify-center gap-1 rounded-[14px] px-1.5 text-sm font-medium min-[400px]:text-base ${
                  active ? 'bg-gold-active' : ''
                }`}
              >
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
