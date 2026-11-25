import { Link } from 'react-router'
import { vociMenu, ordineMobile } from '../data/menu'

/**
 * Menu in basso: si vede SOLO su mobile.
 * "lg:hidden" = da 1024px in su sparisce
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.paginaAttiva
 * @returns {JSX.Element}
 */
export default function BottomNav({ paginaAttiva }) {
  // Rimettiamo le voci nell'ordine mobile (Gioca al centro)
  const voci = ordineMobile.map((id) => vociMenu.find((voce) => voce.id === id))

  return (
    <nav
      aria-label="Menu principale"
      className="fixed inset-x-0 bottom-0 z-10 border-t border-oro bg-sfondo-secondario px-1 pb-2 sm:px-3 lg:hidden"
    >
      <ul className="flex h-20 items-center justify-between">
        {voci.map((voce) => {
          const attiva = voce.id === paginaAttiva
          return (
            <li key={voce.id}>
              <Link
                to={voce.percorso}
                aria-current={attiva ? 'page' : undefined}
                className={`flex h-14 min-w-14 flex-col items-center justify-center gap-1 rounded-[14px] px-1.5 text-sm font-medium min-[400px]:text-base ${
                  attiva ? 'bg-oro-attivo' : ''
                }`}
              >
                <img src={voce.icona} alt="" className="size-6 object-cover" />
                {voce.etichetta}
              </Link>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
