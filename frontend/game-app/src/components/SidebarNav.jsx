import { Link } from 'react-router'
import { vociMenu } from '../data/menu'

/**
 * Menu laterale: si vede SOLO su desktop.
 * "hidden" = nascosto · "lg:flex" = da 1024px in su si vede
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.paginaAttiva
 * @returns {JSX.Element}
 */
export default function SidebarNav({ paginaAttiva }) {
  return (
    <nav
      aria-label="Menu principale"
      className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col gap-2 border-r border-oro bg-sfondo px-4 py-7 lg:flex"
    >
      <p className="mb-6 font-titolo text-h2">Echoes&apos; War</p>

      <ul className="flex flex-col gap-1.5">
        {vociMenu.map((voce) => {
          const attiva = voce.id === paginaAttiva
          return (
            <li key={voce.id}>
              <Link
                to={voce.percorso}
                aria-current={attiva ? 'page' : undefined}
                className={`flex h-12 items-center gap-3 rounded-xl px-3.5 ${
                  attiva ? 'bg-oro-attivo font-semibold' : 'font-medium hover:bg-oro/30'
                }`}
              >
                {/* Barretta verticale: solo sulla voce attiva */}
                {attiva && <span className="h-6 w-1 rounded-sm bg-testo" />}
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
