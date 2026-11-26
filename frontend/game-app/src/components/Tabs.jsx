import { Link } from 'react-router'

/**
 * "Pill" tabs (Carte · Mazzi · Buste, Ricevute · Inviate · Storico...).
 * tabs: [{ id, label, path }] · active: id of the selected tab
 *
 * @param {object} props - the component props
 * @param {{ id: string, label: string, path: string }[]} props.tabs
 * @param {string} props.active
 * @param {string} props.label - accessible name of the tab list
 * @param {string} [props.className='lg:w-105']
 * @returns {JSX.Element}
 */
export default function Tabs({ tabs, active, label, className = 'lg:w-105' }) {
  return (
    <nav aria-label={label} className={`flex w-full rounded-full bg-background-secondary p-1 ${className}`}>
      {tabs.map((tab) => {
        const isSelected = tab.id === active
        return (
          <Link
            key={tab.id}
            to={tab.path}
            aria-current={isSelected ? 'page' : undefined}
            className={`flex min-h-11 flex-1 items-center justify-center rounded-full font-medium ${
              isSelected ? 'border-[1.5px] border-ink bg-gold' : 'hover:bg-background'
            }`}
          >
            {tab.label}
          </Link>
        )
      })}
    </nav>
  )
}
